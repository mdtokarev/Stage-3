# Laba1

Учебный Java/Gradle-проект по предметной области экспериментов, прогонов и результатов прогонов.

Проект разделен на слои:
- `domain` - доменные сущности и их инварианты;
- `service` - in-memory сервисы коллекций и генерация `id`;
- `validation` - ошибки валидации;
- `cli` - консольный интерфейс.

## Что реализовано

На текущем этапе в проекте есть:
- доменные классы `Experiment`, `Run`, `RunResult`, `MeasurementParam`;
- валидация в конструкторах и сеттерах;
- in-memory сервисы с операциями `add/getById/list/update/remove`;
- проверки связей между сущностями на уровне сервисов;
- CLI для создания, просмотра и обновления данных;
- тесты для доменного, сервисного и CLI-слоя.

## Требования

- JDK 17 или выше;
- Gradle Wrapper из репозитория.

## Запуск

Из корня проекта:

```powershell
.\gradlew.bat run
```

При запуске откроется интерактивный CLI.

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

## Архитектурные правила

- доменные классы не вызывают сервисы;
- сервисы создают, ищут и обновляют доменные объекты;
- логика хранения коллекций и генерация `id` находится в сервисах;
- CLI использует сервисы как внешний слой взаимодействия.


