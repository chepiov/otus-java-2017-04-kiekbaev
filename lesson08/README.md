Группа 2017-04-1

## JSON Object Writer

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_


Цель проекта - сериализация Java-объектов в JSON-строку.
JSON составляется из значений полей объектов, getter-ы игнорируются.
Поддерживаются:
* `java.lang.String` поля
* поля примитивных типов и их wrapper-ы 
* массивы и стандартные коллекции объектов (`java.util.Collection`)
* transient поля и аннотация @Transient

Наследование не поддерживается.

Результаты:
```
final Model model = new Model();
model.setFirstField("first");
model.setSecondField(2);
model.setTransientField("transient");

final Otuson otuson = new Otuson();
final String json = otuson.toJson(model);
System.out.println(json);

>> {"firstField":"first","secondField":2}
```
