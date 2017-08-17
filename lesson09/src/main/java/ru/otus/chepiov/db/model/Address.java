package ru.otus.chepiov.db.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.otus.chepiov.db.api.DataSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@Entity
@Table(name = "address")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Address implements DataSet {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "street")
    private String street;
    @Column(name = "index")
    private int index;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address() {
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public int getIndex() {
        return index;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", index=" + index +
                '}';
    }
}
