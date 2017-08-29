Группа 2017-04-1

## Spring integration to Web server

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_


Цель проекта
* Собрать war для приложения из [ДЗ №12](https://github.com/chepiov/otus-java-2017-04-kiekbaev/tree/lesson12/lesson09). Создавать кэш и DBService как Spring beans. Запустить веб приложение
во внешнем веб сервере.
* Создавать один ApplicationContext на приложение. И передавать его в сервлеты параметром.
  
## 
Spring context создается во время старта приложения при помощи `@WebListener`:
 
`ru.otus.chepiov.l13.Starter`

Сервлет авторизации запускается при помощи `@WebServlet`:

`ru.otus.chepiov.l12.LoginServlet`

Фильтр проверки авторизации и сервлет админки добавляются динамически: 

`ru.otus.chepiov.l12.AdminServlet`

`ru.otus.chepiov.l12.LoginFilter`

логин/пароль - admin/admin

