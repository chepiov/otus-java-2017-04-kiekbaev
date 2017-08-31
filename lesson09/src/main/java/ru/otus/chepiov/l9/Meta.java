package ru.otus.chepiov.l9;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Meta information about entities.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
final class Meta {

    private final Class<? extends DataSet> entityClass;
    private final String save;
    private final String load;

    private final String idColumnName;
    private final Map<String, Description> columns;
    private final List<String> columnNames;

    Meta(final Class<? extends DataSet> entityClass) {

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
        final String idColumn = idCandidates.get(0);

        this.load = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        this.idColumnName = idColumn;

        this.columns = Arrays.stream(fields)
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
                    Column column = columnCandidates.get(0);
                    return new Description(column.name(), f, column.insertable(), column.nullable());
                }).collect(Collectors.toMap((r) -> r.column, Function.identity()));
        final ArrayList<String> cn = new ArrayList<>(this.columns.keySet());
        cn.sort(Comparator.naturalOrder());
        this.columnNames = Collections.unmodifiableList(cn);

        this.save = "INSERT INTO " +
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

    Class<? extends DataSet> getEntityClass() {
        return entityClass;
    }

    <T extends DataSet> void save(final T entity, final Connection connection) throws SQLException {

        final PreparedStatement stmt = connection.prepareStatement(save, Statement.RETURN_GENERATED_KEYS);
        final List<String> insertableColumnNames = this.columnNames.stream()
                .filter(c -> this.columns.get(c).insertable)
                .collect(Collectors.toList());
        try {
            for (int i = 1; i <= insertableColumnNames.size(); i++) {
                final String column = insertableColumnNames.get(i - 1);
                final Description description = this.columns.get(column);
                final Field field = description.field;
                final Class<?> type = field.getType();
                field.setAccessible(true);
                final Object value = field.get(entity);
                if (!description.nullable && Objects.isNull(value)) {
                    throw new IllegalArgumentException("Not nullable column but entity field is null. Entity: "
                            + entity.getClass() + ", field: " + field.getName());
                }
                if (type.equals(String.class)) {
                    stmt.setString(i, (String) value);
                } else if (type.equals(Long.class)) {
                    stmt.setLong(i, (Long) value);
                } else if (type.equals(Integer.class)) {
                    stmt.setInt(i, (Integer) value);
                } else if (type.equals(Boolean.class)) {
                    stmt.setBoolean(i, (Boolean) value);
                } else if (type.equals(Short.class)) {
                    stmt.setShort(i, (Short) value);
                } else if (type.equals(Double.class)) {
                    stmt.setDouble(i, (Double) value);
                } else if (type.equals(Float.class)) {
                    stmt.setFloat(i, (Float) value);
                }
                field.setAccessible(false);
            }
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Something wrong", e);
        }

    }

    <T extends DataSet> T load(final Long id, final Connection connection) throws SQLException {
        final PreparedStatement stmt = connection.prepareStatement(this.load);
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();
        try {
            @SuppressWarnings("unchecked")
            T entity = (T) this.entityClass.newInstance();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
                if (cnt > 1) {
                    throw new IllegalArgumentException("Something wrong, multiple return entity");
                }
                entity.setId(rs.getLong(idColumnName));
                for (Description description : this.columns.values()) {
                    final Field field = description.field;
                    final Class<?> type = field.getType();
                    final String column = description.column;
                    field.setAccessible(true);
                    if (type.equals(String.class)) {
                        field.set(entity, rs.getString(column));
                    } else if (type.equals(Long.class)) {
                        field.set(entity, rs.getLong(column));
                    } else if (type.equals(Integer.class)) {
                        field.set(entity, rs.getInt(column));
                    } else if (type.equals(Boolean.class)) {
                        field.set(entity, rs.getBoolean(column));
                    } else if (type.equals(Short.class)) {
                        field.set(entity, rs.getShort(column));
                    } else if (type.equals(Double.class)) {
                        field.set(entity, rs.getDouble(column));
                    } else if (type.equals(Float.class)) {
                        field.set(entity, rs.getFloat(column));
                    }
                    field.setAccessible(false);
                }
            }
            return entity;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class Description {
        private final String column;
        private final Field field;
        private final boolean insertable;
        private final boolean nullable;

        Description(final String column, final Field field, final boolean insertable, final boolean nullable) {
            this.column = column;
            this.field = field;
            this.insertable = insertable;
            this.nullable = nullable;
        }
    }
}
