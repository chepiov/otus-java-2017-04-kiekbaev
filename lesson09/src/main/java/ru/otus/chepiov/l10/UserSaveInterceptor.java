package ru.otus.chepiov.l10;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import ru.otus.chepiov.db.model.User;

import java.io.Serializable;
import java.util.Objects;

/**
 * Association hook for User entity.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class UserSaveInterceptor extends EmptyInterceptor {

    @Override
    public boolean onSave(final Object entity,
                          final Serializable id,
                          final Object[] state,
                          final String[] propertyNames,
                          final Type[] types) {
        if (entity instanceof User) {
            final User user = (User) entity;
            if (Objects.nonNull(user.getPhones())) {
                user.getPhones().forEach(p -> p.setUser(user));
            }
            if (Objects.nonNull(user.getAddress())) {
                user.getAddress().setUser(user);
            }
        }
        return false;
    }
}
