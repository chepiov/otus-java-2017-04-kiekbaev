Группа 2017-04-1

## Garbage Collector и OutOfMemoryError

_**Следует учесть, что данные тесты имеют только учебный характер
и никак не подходят для боевых условий.**_

`ru.otus.chepiov.GarbageProducer` бесконечно генерирует объекты:
* короткоживущие, которые собираются GC
* долгоживущие, которые в итоге вызывают `OutOfMemoryError`

При помощи `GarbageCollectorMXBean` отслеживаются события до `OutOfMemoryError`:
* количество сборок GC в Young Generation
* количество сборок GC в Old Generation
* суммарное время, затраченное на сборку
* среднее время в минуту, затраченное на сборку


Сборка статистики оформлена через `Junit` и `Maven profiles`.

Общие для всех видов GC JVM параметры:

```
-Xmx512m -Xms512m -XX:MaxMetaspaceSize=128m
-verbose:gc -XX:+PrintGCDateStamps
-XX:+PrintGCDetails -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=2M
-Xloggc:./logs/gc_pid_%p.log 
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dumps/
-XX:OnOutOfMemoryError="kill -3 %p"
```

Проверяемые типы GC:

| Параметры JVM  | Young collector | Old collector | Maven profile |
| :-----------:  | :-------------: | :-----------: | :-----------: |
| -XX:+UseSerialGC  | Serial (DefNew) | Serial Mark Sweep Compact | serial |
| -XX:+UseParallelGC | Parallel scavenge | Parallel Mark Sweep Compact | parallel |
| -XX:+UseParNewGC | Parallel (ParNew) | Serial Mark Sweep Compact | parallel-new |
| -XX:+UseConcMarkSweepGC | Parallel (ParNew) | Concurrent Mark Sweep | cms |
| -XX:+UseG1GC | Garbage First (G1)* | Garbage First (G1)* | g1 |

`*` Garbage First работает с регионами Old/Young, а не с областями Old/Young, как другие коллекторы.

Результаты:
```
$ mvn -P serial clean test
...
name: Copy, total count: 7, duration: 11 ms, total duration per last minute: 59 ms, type: end of minor GC, cause: Allocation Failure, free mem: 507009608 bytes
...
name: MarkSweepCompact, total count: 55, duration: 563 ms, total duration per last minute: 11779 ms, type: end of major GC, cause: Allocation Failure, free mem: 111019216 bytes
...
RESULTS:
    TYPE: Copy, COUNT: 337, TOTAL DURATION: 7390 ms, AVERAGE DURATION PER MINUTE: 2273 ms
    TYPE: MarkSweepCompact, COUNT: 74, TOTAL DURATION: 42948 ms, AVERAGE DURATION PER MINUTE: 9706 ms
...
[INFO] Total time: 02:39 min
```
```
$ mvn -P parallel clean test
...
name: PS Scavenge, total count: 10, duration: 7 ms, total duration per last minute: 63 ms, type: end of minor GC, cause: Allocation Failure, free mem: 512067472 bytes
...
name: PS MarkSweep, total count: 20, duration: 1284 ms, total duration per last minute: 27479 ms, type: end of major GC, cause: Ergonomics, free mem: 157779184 bytes
...
RESULTS:
   TYPE: PS Scavenge, COUNT: 270, TOTAL DURATION: 5128 ms, AVERAGE DURATION PER MINUTE: 1657 ms
   TYPE: PS MarkSweep, COUNT: 75, TOTAL DURATION: 103462 ms, AVERAGE DURATION PER MINUTE: 26982 ms
...
[INFO] Total time: 03:33 min
```
```
$ mvn -P parallel-new clean test
...
name: ParNew, total count: 206, duration: 14 ms, total duration per last minute: 3151 ms, type: end of minor GC, cause: Allocation Failure, free mem: 180462880 bytes
...
name: MarkSweepCompact, total count: 63, duration: 566 ms, total duration per last minute: 15374 ms, type: end of major GC, cause: Allocation Failure, free mem: 105787184 bytes
...
RESULTS:
    TYPE: ParNew, COUNT: 337, TOTAL DURATION: 5279 ms, AVERAGE DURATION PER MINUTE: 1750 ms
    TYPE: MarkSweepCompact, COUNT: 74, TOTAL DURATION: 42728 ms, AVERAGE DURATION PER MINUTE: 10261 ms
...
[INFO] Total time: 02:37 min
```
```
$ mvn -P cms clean test
...
name: ParNew, total count: 328, duration: 354 ms, total duration per last minute: 2643 ms, type: end of minor GC, cause: Allocation Failure, free mem: 77770616 bytes
...
name: ConcurrentMarkSweep, total count: 105, duration: 920 ms, total duration per last minute: 86950 ms, type: end of major GC, cause: Allocation Failure, free mem: 79181680 bytes
...
RESULTS:
    TYPE: ParNew, COUNT: 1436, TOTAL DURATION: 1211423 ms, AVERAGE DURATION PER MINUTE: 27317 ms
    TYPE: ConcurrentMarkSweep, COUNT: 1217, TOTAL DURATION: 175458 ms, AVERAGE DURATION PER MINUTE: 172611 ms
...
[INFO] Total time: 42:04 min
```
```
$ mvn -P g1 clean test
...
name: G1 Young Generation, total count: 139, duration: 37 ms, total duration per last minute: 1269 ms, type: end of minor GC, cause: G1 Evacuation Pause, free mem: 250081856 bytes
...
name: G1 Old Generation, total count: 1, duration: 719 ms, total duration per last minute: 719 ms, type: end of major GC, cause: Allocation Failure, free mem: 228905744 bytes
...
RESULTS:
    TYPE: G1 Young Generation, COUNT: 452, TOTAL DURATION: 13114 ms, AVERAGE DURATION PER MINUTE: 2737 ms
    TYPE: G1 Old Generation, COUNT: 3, TOTAL DURATION: 2759 ms, AVERAGE DURATION PER MINUTE: 0 ms
...
[INFO] Total time: 04:05 min
```
### Автор 
Anvar Kiekbaev (Анвар Киекбаев)

a.kiekbaev@chepiov.org