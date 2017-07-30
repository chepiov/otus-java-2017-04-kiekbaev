package ru.otus.chepiov.db.model;

import ru.otus.chepiov.db.api.DataSet;

import javax.persistence.*;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@Entity
@Table(name = "phone")
public class Phone implements DataSet {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private int code;
    @Column(name = "number")
    private String number;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public Long getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    public String getNumber() {
        return number;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "id=" + id +
                ", code=" + code +
                ", number='" + number + '\'' +
                '}';
    }
}
