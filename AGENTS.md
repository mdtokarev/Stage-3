# Заметки по проекту Laba1

## Проект
- Путь: `C:\Users\maksi\.codex\Laba1`
- Язык и сборка: Java, Gradle
- Методичка: `C:\Users\maksi\.codex\Laba1\Методика.pdf`
- Предметная область: эксперименты, прогоны, результаты прогонов

## Что входит в этап 1
- Доменные классы: `Experiment`, `Run`, `RunResult`, `MeasurementParam`
- Валидация в конструкторах и сеттерах
- In-memory сервисы коллекций с методами `add/getById/list/update/remove`
- Логика коллекций и генерация `id` должны быть в сервисах
- CLI относится к этапу 2, но код этапа 1 уже должен быть пригоден для вызова из команд

## Текущая структура пакетов
- `src/main/java/domain`
- `src/main/java/service`
- `src/main/java/util`
- `src/main/java/validation`
- `src/main/java/cli`
- `src/main/java/storage`
- `src/main/java/ui`
- `src/test/java/domain`
- `src/test/java/service`
- `src/test/java/cli`

## Текущее состояние на 2026-04-11
- Генерация `id` вынесена в сервисы, конструкторы сущностей принимают `id` извне
- `RunService` проверяет существование `Experiment` перед созданием `Run`
- `RunResultService` проверяет существование `Run` перед созданием `RunResult`
- Обновления в сервисах идут через атомарные методы `update(...)` в доменных классах
- Добавлены `listByExperimentId(...)` и `listByRunId(...)`
- Для `RunResult.unit` добавлена проверка длины до 16 символов
- Исправлен сценарий `RunResult.setParam(...)`, при котором объект мог частично портиться
- Тесты разнесены по слоям: доменные тесты лежат в `src/test/java/domain`, сервисные в `src/test/java/service`
- Проверки корректной генерации `id` перенесены в тесты сервисов
- `IdGeneratorTest` удален как не отражающий реальный контракт сервисного слоя
- `./gradlew test` проходит успешно
- Добавлен `cli.CliRunner` с циклом чтения команд
- `Main` делегирует запуск в `CliRunner.run()`
- Реализованы команды `help`, `exit`, `exp_add`, `exp_list`, `exp_show`, `exp_update`
- Реализованы команды `run_add`, `run_list`, `run_show`
- Реализованы команды `res_add`, `res_list`
- Реализована команда `exp_summary`
- Для `exp_add`, `run_add`, `res_add` используется интерактивный ввод полей
- Для `exp_update` принят упрощенный контракт: одна команда обновляет одно поле в формате `exp_update <id> field=value`
- `res_list` поддерживает необязательный фильтр `--param`
- `exp_summary` считает `count/min/max/avg` по каждому `MeasurementParam` простыми циклами
- Добавлены CLI-тесты на все основные сценарии этапа 2
- Для stage 3 добавлены `Experiment.restore(...)`, `Run.restore(...)`, `RunResult.restore(...)`
- Сервисы теперь сами держат `nextId` и умеют `loadRestored(...)` с пересчетом `maxId + 1`
- Реализованы `service.DataManager`, file DTO, `storage.persistence.file.JsonFileStorage`, `FileValidator`, `FileSnapshotMapper`
- Реализованы команды CLI `save <path>` и `load <path>`
- `save/load` работают через схему `services -> DTO -> JSON` и `JSON -> DTO -> validate -> restore -> temp services -> replace`
- Добавлены тесты stage 3: validator, mapper/restore, integration round-trip, `nextId` после `load`, защита от битого файла, CLI `save/load`
- `./gradlew test` проходит успешно после реализации stage 3
- `exp_summary` вынесен в отдельный `ExperimentSummaryService`, чтобы CLI и UI использовали одну и ту же логику
- Для stage 4 добавлены `ui.UiMain`, `ui.UiLauncher`, `ui.controller.MainController`, `ui.view.MainView`, `ui.dialog`, `ui.mapper`, `ui.viewmodel`
- JavaFX UI запускается отдельной Gradle-задачей `runUi`, не ломая существующий CLI entry point
- Реализован master-detail UI с тремя таблицами `Experiment -> Run -> RunResult`, панелью деталей, фильтром по `MeasurementParam` и кнопками `Save/Load/Refresh`
- Через UI можно создавать и редактировать `Experiment`, `Run`, `RunResult`
- UI показывает summary эксперимента через сервисный слой и использует `DataManager` для `save/load`
- `README.md` обновлен под запуск CLI и JavaFX UI
- `./gradlew test` проходит успешно после реализации stage 4

## Правило по этапу 2
- Дальше идти по списку команд из `Методика.pdf`, а не придумывать свой порядок
- Команды из методички по сути уже покрыты, кроме отличия в имени `exp_add` vs `exp_create`
- При защите нужно явно проговорить, что архитектура разделена на `domain`, `service`, `validation`, `cli`

## Что еще осталось
- При желании довести UI-полировку: форматирование дат, более богатые пустые состояния, delete-сценарии после отдельного сервисного контракта
- При желании привести названия CLI-команд ближе к формулировкам из методички
- Поддерживать заметки и защитные материалы в актуальном состоянии

## Пометка по ЛР2 на 2026-04-16
- Этап 3 по методичке: файловое хранение (`storage/persistence/file`), команда `load <path>`, отдельные `FileStorage` и `FileValidator`
- Этап 4 по методичке: `JavaFX UI`
- В `pull/2` у напарника есть только старт этапа 3: DTO для JSON (`DataSnapshot`, `ExperimentRecord`, `RunRecord`, `RunResultRecord`), пустой `FileStorage`, частичный `FileValidator`
- Ключевой риск этапа 3: текущий домен не умеет восстанавливать `createdAt/updatedAt` из файла, а загрузка должна быть согласована с уже сделанными `domain/service/cli`
- Рекомендуемый порядок: сначала согласовать restore-контракт домена и сервисов, потом доделать JSON `load`, и только после этого делать этап 4 с UI поверх тех же сервисов

## Правило по этапу 3 на 2026-04-19
- Stage 3 строить поверх этапов 1 и 2, не ломая их архитектуру
- Не сериализовать доменные объекты напрямую в JSON и не десериализовать JSON сразу в домен
- Целевая схема: `services -> domain -> file DTO -> JSON` и обратно `JSON -> file DTO -> validate -> restore domain -> load into services`
- Для файлового формата держать отдельные DTO: `DataSnapshot`, `ExperimentRecord`, `RunRecord`, `RunResultRecord`
- `FileStorage` отвечает только за чтение и запись `DataSnapshot`, без бизнес-логики и без восстановления доменных объектов
- `FileValidator` валидирует именно DTO до загрузки: секции, обязательные поля, уникальность `id`, корректность ссылок, enum, даты и доменные ограничения
- Домену нужен restore-контракт: `Experiment.restore(...)`, `Run.restore(...)`, `RunResult.restore(...)` с сохранением `id`, `createdAt`, `updatedAt`
- Сервисы при restore не генерируют новые `id`, пересчитывают `nextId` как `maxId + 1` и заменяют данные только атомарно после полной успешной проверки
- `DataManager` оставить фасадом, но собрать через сценарии `save: services -> mapper -> snapshot -> file` и `load: file -> snapshot -> validator -> mapper/restore -> temp services -> replace`
- JSON должен хранить даты как ISO-8601 строки, а связи задавать через `experimentId` и `runId`
- Текущий stage 3 считать черновиком: с высокой вероятностью полностью переписывать `JsonFileStorage`, `JsonDataValidator`, `DataManager` и доводить DTO/mapper слой
- Stage 3 считать готовым только если `save/load` реально восстанавливают данные, связи и timestamps, продолжают последовательность `id`, ловят битый JSON понятной ошибкой и не портят текущие данные при неудачной загрузке
- Обязательные тесты этапа 3: unit для `FileValidator`, unit для mapper/restore, integration для round-trip и `nextId` после `load`, защита от битого файла, CLI-тесты для `save/load`

## Архитектурное правило
- Доменные классы не должны вызывать сервисы
- Сервисы создают, ищут и обновляют доменные объекты
- Доменные классы отвечают только за собственные инварианты и состояние

## Правило ведения файла
- Держать файл коротким и актуальным
- Обновлять после значимых архитектурных или статусных изменений
- Подробная история реализованных изменений ведется в `C:\Users\maksi\.codex\Laba1\laba-plans.md`
