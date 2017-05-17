Группа 2017-04-1

## Test framework

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_

Проект состоит из:
* тестового фреймворка: модуль `testframework`
* maven-плагина для запуска фреймворка: модуль `testplugin`
* тестируемого кода: модуль `testcode`


Для работы плагина он должен быть доступен в репозитории maven, поэтому фаза `install` необходима:
```bash
$ mvn clean install
```
К сожалению, не нашёл обходного пути, придется немного засорить локальный репозиторий :)

#### Фреймворк
Фреймворк поддерживает аннотации:
* `ru.otus.chepiov.tf.Before`
* `ru.otus.chepiov.tf.Test`
* `ru.otus.chepiov.tf.After`

Так же вспомогательные assert-ы:
* `ru.otus.chepiov.tf.Assert#assertTrue(boolean condition)`
* `ru.otus.chepiov.tf.Assert#assertTrue(boolean condition, String message)`
* `ru.otus.chepiov.tf.Assert#assertFalse(boolean condition)`
* `ru.otus.chepiov.tf.Assert#assertFalse(boolean condition, String message)`
* `ru.otus.chepiov.tf.Assert#assertNotNull(boolean condition)`
* `ru.otus.chepiov.tf.Assert#assertNotNull(boolean condition, String message)`

Запуск фреймворка через:
* `ru.otus.chepiov.tf.Runner#run(String packageName)`
* `ru.otus.chepiov.tf.Runner#run(Class[] classNames)`

Следует учесть, что метод с параметром имени пакета ищет аннотированные методы при помощи `classloader`а 
текущего потока.

Фреймворк поддерживает только аннотации на методе и не поддерживает наследуемость аннотаций.

_Примечание_:
Самым нетривиальным оказался поиск классов в уже собранном `jar`-файле - познакомился с `java.nio.file.FileSystem`.


#### Плагин
Подключение плагина:
```
<plugin>
    <groupId>ru.otus.chepiov</groupId>
    <artifactId>testplugin</artifactId>
    <executions>
        <execution>
            <phase>test</phase>
            <goals>
                <goal>run-tests</goal>
            </goals>
            <configuration>
                <packageName>${packageName}</packageName>
                <classNames>
                    <param>${firstTestClass}</param>
                    <param>${secondTestClass}</param>
                    ...
                </classNames>
            </configuration>
        </execution>
    </executions>
</plugin>
```
_Примечание_:
довольно интересно было разбираться с classloader-ом в реализации плагина - собирать вручную все классы и 
создавать `classloader` для тестируемого кода. Например `URLClassLoader` не видит аннотации у классов, которые 
собирались для тестирования, если ему не указать родительский `classloader`. 

#### Результат работы
Запуск напрямую (тесты падают):

```
$ java -jar testcode/target/testcode.jar -p ru.otus.chepiov.l5.failure
Exception in thread "main" java.lang.AssertionError: Tests failed! 
class: ru.otus.chepiov.l5.failure.AppTestFailure
	 Methods: 
		[testFailedFromAssertTrue: The Ultimate Question of Life, the Universe, and Everything must be 42] 
		[testFailedFromAssertFalse: Something wrong! What the hell???] 
		[testFailedFromAssertNotNull: AGHHHH!] 
	at ru.otus.chepiov.tf.Runner.run(Runner.java:90)
	at ru.otus.chepiov.tf.Runner.run(Runner.java:127)
	at ru.otus.chepiov.l5.AppTestRunner.main(AppTestRunner.java:28)
```
Запуск напрямую (тесты проходят):
```
$ java -jar testcode/target/testcode.jar -c ru.otus.chepiov.l5.success.AppTestSuccess
$
```

Запуск через maven-плагин (тесты падают):
```
$ mvn clean install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] lesson05
[INFO] testframework
[INFO] testplugin
[INFO] testcode
...
[INFO] --- testplugin:1.0:run-tests (default) @ testcode ---
[INFO] -------------------------------------------------------
[INFO] OTUS TESTS
[INFO] -------------------------------------------------------
[INFO] 
[INFO] Class names: [ru.otus.chepiov.l5.AppTest]
[INFO] Package name: ru.otus.chepiov.l5.failure
[INFO] Class names in package: [class ru.otus.chepiov.l5.failure.AppTestFailure]
[INFO] Results:
[ERROR] Tests failed! 
class: ru.otus.chepiov.l5.failure.AppTestFailure
         Methods: 
                [testFailedFromAssertTrue: The Ultimate Question of Life, the Universe, and Everything must be 42] 
                [testFailedFromAssertFalse: Something wrong! What the hell???] 
                [testFailedFromAssertNotNull: AGHHHH!] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] lesson05 ........................................... SUCCESS [  0.237 s]
[INFO] testframework ...................................... SUCCESS [  1.510 s]
[INFO] testplugin ......................................... SUCCESS [  0.695 s]
[INFO] testcode ........................................... FAILURE [  0.155 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
...
```
Запуск через maven-плагин (тесты проходят):
```
$ mvn clean install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] lesson05
[INFO] testframework
[INFO] testplugin
[INFO] testcode
...
[INFO] --- testplugin:1.0:run-tests (default) @ testcode ---
[INFO] -------------------------------------------------------
[INFO] OTUS TESTS
[INFO] -------------------------------------------------------
[INFO] 
[INFO] Class names: [ru.otus.chepiov.l5.AppTest]
[INFO] Package name: ru.otus.chepiov.l5.failure
[INFO] Class names in package: [class ru.otus.chepiov.l5.failure.AppTestFailure]
[INFO] Results:
[INFO] All tests passed
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] lesson05 ........................................... SUCCESS [  0.182 s]
[INFO] testframework ...................................... SUCCESS [  1.410 s]
[INFO] testplugin ......................................... SUCCESS [  0.833 s]
[INFO] testcode ........................................... SUCCESS [  2.214 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Автор 
Anvar Kiekbaev (Анвар Киекбаев)

a.kiekbaev@chepiov.org