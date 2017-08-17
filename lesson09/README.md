Группа 2017-04-1

## Cache engine

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_


Цель проекта
* Написать свой cache engine с soft references.
* Добавить кэширование в DBService из заданий [9](https://github.com/chepiov/otus-java-2017-04-kiekbaev/tree/master/lesson09)
 и [10](https://github.com/chepiov/otus-java-2017-04-kiekbaev/tree/lesson10/lesson09)

## 
`ru.otus.chepiov.l11.CacheEngine` - интерфейс cache engine

`ru.otus.chepiov.l11.SoftRefCacheEngine` - реализация

`ru.otus.chepiov.l11.SoftRefCacheEngineMBean` - MBean интерфейс для cache engine

Так же добавлен `Ehcache` как L2-cache для имплементации DBService на основе `Hibernate`.