Группа 2017-04-1

## Web server

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_


Цель проекта
* Встроить веб сервер в приложение из задания [11](https://github.com/chepiov/otus-java-2017-04-kiekbaev/tree/lesson11/lesson09)
* Сделать админскую страницу, на которой админ должен авторизоваться и получить доступ к параметрам и
  состоянию кэша.
  
## 
`ru.otus.chepiov.l9.test.RunWebServer` - раннер сервера

`ru.otus.chepiov.l12.AdminServlet` - асинхронный сервлет админки
`ru.otus.chepiov.l12.LoginServlet` - сервлет авторизации
`ru.otus.chepiov.l12.LoginFilter` - фильтр проверки авторизации

логин/пароль - admin/admin

В качестве template-engine используется Jade4j.