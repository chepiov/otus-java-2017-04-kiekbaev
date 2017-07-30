package ru.otus.chepiov.l10.dao;

import org.hibernate.Session;
import ru.otus.chepiov.db.model.User;

/**
 * User data access object.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class UserDAO {
    private final Session session;


    public UserDAO(final Session session) {
        this.session = session;
    }

    public User getUser(final Long id) {
        return session.load(User.class, id);
    }

    public void saveUser(final User user) {
        session.save(user);
    }
}
