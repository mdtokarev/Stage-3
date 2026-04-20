# Laba1

Учебный Java/Gradle-проект по предметной области экспериментов, прогонов и результатов прогонов.

Проект разделен на слои:
- `domain` - доменные сущности и их инварианты;
- `service` - in-memory сервисы коллекций и генерация `id`;
- `validation` - ошибки валидации;
- `cli` - консольный интерфейс.
- `storage` - файловое хранение JSON и загрузка snapshot;
- `ui` - JavaFX интерфейс поверх сервисного слоя.

## Что реализовано

На текущем этапе в проекте есть:
- доменные классы `Experiment`, `Run`, `RunResult`, `MeasurementParam`;
- валидация в конструкторах и сеттерах;
- in-memory сервисы с операциями `add/getById/list/update/remove`;
- проверки связей между сущностями на уровне сервисов;
- CLI для создания, просмотра и обновления данных;
- JSON `save/load` через `DataManager`;
- JavaFX UI с тремя связанными таблицами `Experiment -> Run -> RunResult`;
- формы создания и редактирования сущностей в UI;
- показ summary эксперимента через сервисный слой;
- тесты для доменного, сервисного, CLI и persistence-слоя.

## Требования

- JDK 17 или выше;
- Gradle Wrapper из репозитория.

## Запуск CLI

Из корня проекта:

```powershell
.\gradlew.bat run
```

При запуске откроется интерактивный CLI.

## Запуск JavaFX UI

```powershell
.\gradlew.bat runUi
```

UI открывает:
- таблицу экспериментов;
- таблицу прогонов выбранного эксперимента;
- таблицу результатов выбранного прогона;
- панель деталей;
- действия `Save`, `Load`, `Refresh`, `Show Summary`;
- диалоги создания и редактирования сущностей.

## Запуск тестов

```powershell
.\gradlew.bat test
```

## Доступные команды

```text
help
exit
exp_add
exp_list
exp_show <id>
exp_update <id> field=value
exp_summary <id>
run_add <experimentId>
run_list <experimentId>
run_show <runId>
res_add <runId>
res_list <runId> [--param PARAM]
```

## Поддерживаемые поля и значения

`exp_update` поддерживает поля:
- `name`
- `description`
- `ownerUsername`

`res_list --param` и создание результата поддерживают параметры измерения:
- `pH`
- `Temperature`
- `Concentration`

## Как работает интерактивный ввод

Команды `exp_add`, `run_add` и `res_add` после ввода команды запрашивают поля по одному.

Пример:

```text
> exp_add
Creating a new experiment.
Name: Water test
Description: First batch
Owner username: maks
Experiment created with id 1
```

## Пример сценария

```text
> exp_add
> run_add 1
> res_add 1
> exp_list
> run_list 1
> res_list 1
> exp_summary 1
```

## Пример ручной проверки UI

1. Запустить `.\gradlew.bat runUi`
2. Создать `Experiment`
3. Создать `Run` для выбранного эксперимента
4. Создать несколько `RunResult`
5. Открыть `Show Summary`
6. Сохранить в JSON
7. Перезапустить UI
8. Загрузить JSON через `Load`
9. Проверить, что связи и генерация `id` продолжаются корректно

## Архитектурные правила

- доменные классы не вызывают сервисы;
- сервисы создают, ищут и обновляют доменные объекты;
- логика хранения коллекций и генерация `id` находится в сервисах;
- CLI использует сервисы как внешний слой взаимодействия.

