package ru.otus.chepiov.l10.dao;

import org.hibernate.Session;
import ru.otus.chepiov.db.api.DAO;
import ru.otus.chepiov.db.model.User;

/**
 * User data access object.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class UserDAO implements DAO<User> {
    private final Session session;


    public UserDAO(final Session session) {
        this.session = session;
    }

    @Override
    public User load(final Long id) {
        return session.load(User.class, id);
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public void save(final User user) {
        session.save(user);
    }
}
