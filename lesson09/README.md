Группа 2017-04-1

## Simple JPA ORM and Hibernate

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_


Цель проекта
* выделить интерфейс `DBService` для сохранения в базе данных сущностей вида:
```
class Address {
    private String street;
    private int index;
}
class Phone {
    private int code;
    private String number;
}
class User {
    private String name;
    private Integer age;
    private Address address;
    private List<Phone> phones;
}
```
User содержит один Address (_one-to-one_) и много телефонов (_one-to-many_)
* реализовать упрощенную, с минимальным функционалом, ORM с поддержкой JPA аннотаций и реализовать на 
основе неё интерфейс `DBService`
* реализовать интерфейс `DBService` на основе `Hibernate 5.2`

`ru.otus.chepiov.l9.Executor` - реализация `DBService`, принимает в конструкторе информацию о базе данных,
а так же классы `persistence` сущностей. `Persistence` сущности 
должны реализовывать интерфейс `ru.otus.db.api.DataSet`.
 
`ru.otus.chepiov.l9.Executor` обрабатывает классы, создает meta-информацию о них
и составляет `sql` запросы на создание сущности и получение сущности по `id`. 
Meta-инфромация берётся из JPA-аннотаций:
* @Table
* @Id
* @Column
* @OneToMany
* @OneToOne
* @JoinColumn

Есть проверка на атрибуты `insertable` и `nullable` у аннотации `@Column`. `OneToX` связи обрабатываются только 
`bi-directional`, то есть на стороне сущности, содержащей внешний ключ должна быть задана корректная аннотация @JoinColumn
с обязательным параметром `name`. Поддержки связывающей таблицы (`cross-table`) нет.

Используется простейший connection pool - `BlockingQueue` с предустановленными соединениями. Соединения проксируются при помощи
`Proxy.newProxyInstance`, при занятости всех соединений происходит ожидание в 5 секунд и если неудачно, то выбрасывается исключение.

Связанные таблицы запрашиваются отдельными запросами, а не через `JOIN`.

Поддерживается обработка примитивных типов и их обёрток.

`ru.otus.chepiov.l10.HibernateDBService` - реализация на основе `Hibernate`. Ассоциация связей при каскадном 
сохранении сущности `User` поддерживается при помощи интерсептора `ru.otus.chepiov.l10.UserSaveInterceptor`.

Тестирование проводилось на `in-memory h2-database`, тестовые данные заполняются при помощи `liquibase`.
Для использования одного и того же теста конкретная реализация сервиса `DBService` задается через `Parameterized` 
runner JUnit.
 