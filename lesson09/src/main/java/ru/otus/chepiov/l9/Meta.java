package ru.otus.chepiov.l9;

import ru.otus.chepiov.db.api.DataSet;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Meta information about entities.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
final class Meta<T extends DataSet> {

    private final Class<T> entityClass;
    private final String saveQuery;
    private final String loadQuery;
    private final String loadByRelationQuery;

    private final String idColumnName;
    private final Map<String, Description> columns;
    private final List<String> columnNames;

    private final Map<Class<?>, RelDescription> ownerRelations;
    private final Set<Field> oneToManyFields;
    private final Set<Field> oneToOneFields;

    private Map<Class<? extends DataSet>, Meta<?>> metas;

    private static final String RELATION_COL_PAT = "#rel_col#";

    private Meta(final Class<T> entityClass) {

        this.entityClass = entityClass;

        final List<String> tableCandidates = Arrays.stream(entityClass.getDeclaredAnnotations())
                .filter(a -> a instanceof Table)
                .map(a -> (Table) a)
                .map(Table::name)
                .collect(Collectors.toList());
        if (tableCandidates.size() != 1) {
            throw new RuntimeException("Illegal @Table annotation on entity: " + entityClass.getName());
        }
        final String tableName = tableCandidates.get(0);

        final Field[] fields = entityClass.getDeclaredFields();

        final String idColumn = getIdColumn(entityClass, fields);

        this.loadQuery = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        this.loadByRelationQuery = "SELECT * FROM " + tableName + " WHERE " + RELATION_COL_PAT + " = ?";

        this.idColumnName = idColumn;

        this.ownerRelations = getOwnerRelationDescription(entityClass, fields);
        this.oneToManyFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(OneToMany.class))
                .collect(Collectors.toSet());
        this.oneToOneFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(OneToOne.class))
                .filter(f -> !f.isAnnotationPresent(JoinColumn.class))
                .collect(Collectors.toSet());

        this.columns = getColumns(entityClass, fields, ownerRelations);

        final ArrayList<String> cn = new ArrayList<>(this.columns.keySet());
        cn.sort(Comparator.naturalOrder());
        this.columnNames = Collections.unmodifiableList(cn);

        this.saveQuery = "INSERT INTO " +
                tableName +
                "(" +
                String.join(", ", this.columnNames.stream()
                        .filter(c -> this.columns.get(c).insertable)
                        .collect(Collectors.toList())) +
                ") VALUES (" +
                String.join(",", this.columnNames.stream()
                        .filter(c -> this.columns.get(c).insertable)
                        .map(c -> "?")
                        .collect(Collectors.toList())) +
                ")";
    }

    static <T extends DataSet> void buildAndSaveMeta(final Class<T> entityClass,
                                                     final Map<Class<? extends DataSet>, Meta<?>> metas) {
        final Meta<T> meta = new Meta<>(entityClass);
        metas.put(entityClass, meta);
        meta.metas = metas;
    }

    @SuppressWarnings("ConstantConditions")
    void save(final T entity,
              final Connection connection)
            throws SQLException {

        final PreparedStatement stmt = connection.prepareStatement(saveQuery, Statement.RETURN_GENERATED_KEYS);
        final List<String> insertableColumnNames = this.columnNames.stream()
                .filter(c -> this.columns.get(c).insertable)
                .collect(Collectors.toList());
        try {
            for (int i = 1; i <= insertableColumnNames.size(); i++) {
                final String column = insertableColumnNames.get(i - 1);
                final Description description = this.columns.get(column);
                final Field field = description.field;
                final Class<?> type;
                final Object value;
                field.setAccessible(true);
                if (description.relational) {
                    type = Long.class;
                    value = getEntityId((DataSet) field.get(entity));
                } else {
                    type = field.getType();
                    value = field.get(entity);
                }

                if (!description.nullable && Objects.isNull(value)) {
                    throw new IllegalArgumentException("Not nullable column but entity field is null. Entity: "
                            + entity.getClass() + ", field: " + field.getName());
                }
                switch (Types.fromClass(type)) {
                    case STRING:
                        stmt.setString(i, (String) value);
                        break;
                    case LONG:
                        stmt.setLong(i, (Long) value);
                        break;
                    case INT:
                        stmt.setInt(i, (Integer) value);
                        break;
                    case BOOLEAN:
                        stmt.setBoolean(i, (Boolean) value);
                        break;
                    case SHORT:
                        stmt.setShort(i, (Short) value);
                        break;
                    case FLOAT:
                        stmt.setFloat(i, (Float) value);
                        break;
                    case DOUBLE:
                        stmt.setDouble(i, (Double) value);
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal type");

                }
                field.setAccessible(false);
            }
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                final long id;
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                    entity.setId(id);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }

                for (final Field f : oneToOneFields) {
                    f.setAccessible(true);
                    final Object relEntity = f.get(entity);
                    saveRel(entity, connection, f, relEntity);
                    f.setAccessible(false);
                }

                for (Field f : oneToManyFields) {
                    f.setAccessible(true);
                    final List<?> relEntities = (List<?>) f.get(entity);
                    if (Objects.nonNull(relEntities)) {
                        for (final Object relEntity : relEntities) {
                            saveRel(entity, connection, f, relEntity);
                        }
                    }

                }
            } catch (NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalArgumentException("Something wrong", e);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Something wrong", e);
        }

    }

    T load(final Long id,
           final Connection connection)
            throws SQLException {
        final PreparedStatement stmt = connection.prepareStatement(this.loadQuery);
        stmt.setLong(1, id);
        final ResultSet rs = stmt.executeQuery();
        final List<T> extract = extract(rs);
        if (extract.size() != 1) {
            throw new RuntimeException("Multiple entities found");
        }
        final T result = extract.get(0);
        this.oneToOneFields.forEach(f -> {
            @SuppressWarnings("SuspiciousMethodCalls") final Meta<?> relMeta = metas.get(f.getType());
            try {
                final List<? extends DataSet> relations = relMeta.loadByOwning(id, entityClass, connection);
                if (relations.size() > 1) {
                    throw new RuntimeException("Multiple entities found for One-to-One relation");
                }
                if (relations.size() == 1) {
                    f.setAccessible(true);
                    final Object relation = relations.get(0);
                    f.set(result, relation);
                    f.setAccessible(false);
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
                System.out.println("Unable to load One-to-One relation");
            }
        });
        this.oneToManyFields.forEach(f -> {
            ParameterizedType collectionType = (ParameterizedType) f.getGenericType();

            @SuppressWarnings("SuspiciousMethodCalls") final Meta<?> relMeta =
                    metas.get(collectionType.getActualTypeArguments()[0]);
            try {
                final List<? extends DataSet> relations = relMeta.loadByOwning(id, entityClass, connection);
                if (relations.size() > 0) {
                    f.setAccessible(true);
                    f.set(result, relations);
                    f.setAccessible(false);
                }

            } catch (Exception ignore) {
                ignore.printStackTrace();
                System.out.println("Unable to load One-to-Many relation");
            }
        });
        return result;
    }

    private void saveRel(T entity, Connection connection, Field f, Object relEntity) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (Objects.nonNull(relEntity)) {
            @SuppressWarnings("SuspiciousMethodCalls") final Meta<?> relMeta = metas.get(relEntity.getClass());
            final RelDescription relDesc = relMeta.ownerRelations.get(entity.getClass());
            final Field relF = relDesc.columnField;
            relF.setAccessible(true);
            relF.set(relEntity, entity);
            relF.setAccessible(false);
            f.setAccessible(false);
            Method m = relMeta.getClass().getDeclaredMethod("save", DataSet.class, Connection.class);
            //noinspection JavaReflectionInvocation
            m.invoke(relMeta, relEntity, connection);
        }
    }

    private List<T> loadByOwning(final Long relId,
                                 final Class<? extends DataSet> relEntityClass,
                                 final Connection connection)
            throws SQLException {
        final RelDescription relColumn = this.ownerRelations.get(relEntityClass);
        if (Objects.isNull(relColumn)) {
            throw new RuntimeException("Illegal relational loading, unknown relation class: " + relEntityClass.getName());
        }
        final PreparedStatement stmt = connection.prepareStatement(
                this.loadByRelationQuery.replace(RELATION_COL_PAT,
                        relColumn.columnName));
        stmt.setLong(1, relId);
        ResultSet rs = stmt.executeQuery();
        return extract(rs);
    }

    private List<T> extract(ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        try {
            while (rs.next()) {
                @SuppressWarnings("unchecked")
                T entity = this.entityClass.newInstance();
                entity.setId(rs.getLong(idColumnName));
                for (Description description : this.columns.values()) {
                    if (!description.relational) {
                        final Field field = description.field;
                        final Class<?> type = field.getType();
                        final String column = description.column;
                        field.setAccessible(true);
                        switch (Types.fromClass(type)) {
                            case STRING:
                                field.set(entity, rs.getString(column));
                                break;
                            case LONG:
                                field.set(entity, rs.getLong(column));
                                break;
                            case INT:
                                field.set(entity, rs.getInt(column));
                                break;
                            case BOOLEAN:
                                field.set(entity, rs.getBoolean(column));
                                break;
                            case SHORT:
                                field.set(entity, rs.getShort(column));
                                break;
                            case DOUBLE:
                                field.set(entity, rs.getDouble(column));
                                break;
                            case FLOAT:
                                field.set(entity, rs.getFloat(column));
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal type of field");
                        }
                        field.setAccessible(false);
                    }
                }
                result.add(entity);
            }
            return result;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getIdColumn(final Class<? extends DataSet> entityClass, final Field[] fields) {
        final List<Field> idFieldCandidates = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(Id.class) && f.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());
        if (idFieldCandidates.size() != 1) {
            throw new RuntimeException("Illegal @Id annotation on entity: " + entityClass.getName());
        }
        final List<String> idCandidates = Arrays.stream(idFieldCandidates.get(0).getAnnotations())
                .filter(a -> a instanceof Column)
                .map(a -> (Column) a)
                .map(Column::name)
                .collect(Collectors.toList());
        if (idCandidates.size() != 1) {
            throw new RuntimeException("Illegal @Id annotation on entity: " + entityClass.getName());
        }
        return idCandidates.get(0);
    }

    private static Map<String, Description> getColumns(final Class<? extends DataSet> entityClass,
                                                       final Field[] fields,
                                                       final Map<Class<?>, RelDescription> ownerRelations) {

        Map<String, Description> relational = Arrays.stream(fields)
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(JoinColumn.class))
                .map(f -> new Description(
                        ownerRelations.get(f.getType()).columnName,
                        f,
                        true,
                        false,
                        true))
                .collect(Collectors.toMap((r) -> r.column, Function.identity()));

        Map<String, Description> simple = Arrays.stream(fields)
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> {
                    final List<Column> columnCandidates = Arrays.stream(f.getAnnotations())
                            .filter(a -> a instanceof Column)
                            .map(a -> (Column) a)
                            .collect(Collectors.toList());
                    if (columnCandidates.size() != 1) {
                        throw new RuntimeException("Illegal @Column annotation on entity: " + entityClass.getName()
                                + ", field: " + f.getName());
                    }
                    final Column column = columnCandidates.get(0);
                    return new Description(column.name(), f, column.insertable(), column.nullable(), false);
                }).collect(Collectors.toMap((r) -> r.column, Function.identity()));
        relational.putAll(simple);
        return relational;
    }

    private static Map<Class<?>, RelDescription> getOwnerRelationDescription(final Class<? extends DataSet> entityClass,
                                                                             final Field[] fields) {
        final Map<Class<?>, RelDescription> result = new HashMap<>();
        Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(JoinColumn.class))
                .forEach(f -> {
                    final List<JoinColumn> columnCandidates = Arrays.stream(f.getAnnotations())
                            .filter(a -> a instanceof JoinColumn)
                            .map(a -> (JoinColumn) a)
                            .collect(Collectors.toList());
                    if (columnCandidates.size() != 1) {
                        throw new RuntimeException("Illegal @OneToMany annotation on entity: " + entityClass.getName()
                                + ", field: " + f.getName());
                    }
                    final JoinColumn column = columnCandidates.get(0);
                    final String foreignKeyColumnName = column.name();
                    final Class<?> foreignClass = f.getType();
                    if (Objects.nonNull(result.putIfAbsent(foreignClass, new RelDescription(foreignKeyColumnName, f)))) {
                        throw new RuntimeException("Many @OneToMany annotations for same relation on entity: "
                                + entityClass.getName()
                                + ", field: " + f.getName());
                    }
                });
        return result;
    }

    private static <E extends DataSet> Long getEntityId(final E entity) {
        final List<Field> idFieldCandidates = Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class) && f.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());
        if (idFieldCandidates.size() != 1) {
            throw new RuntimeException("Illegal @Id annotation on entity: " + entity.getClass().getName());
        }
        idFieldCandidates.get(0).setAccessible(true);
        try {
            return (Long) idFieldCandidates.get(0).get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Something wrong", e);
        } finally {
            idFieldCandidates.get(0).setAccessible(false);
        }

    }

    private static final class Description {
        private final String column;
        private final Field field;
        private final boolean insertable;
        private final boolean nullable;
        private final boolean relational;

        Description(final String column,
                    final Field field,
                    final boolean insertable,
                    final boolean nullable,
                    final boolean relational) {
            this.column = column;
            this.field = field;
            this.insertable = insertable;
            this.nullable = nullable;
            this.relational = relational;
        }
    }

    private static final class RelDescription {
        final String columnName;
        final Field columnField;

        RelDescription(String columnName, Field columnField) {
            this.columnName = columnName;
            this.columnField = columnField;
        }
    }
}
