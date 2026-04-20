# Журнал изменений Laba1

В этом файле фиксируются реализованные изменения и рабочие предложения по проекту.

## Формат записи
- Суть изменения
- Файл или файлы
- Почему сделано именно так
- Финальный код

---

## 2026-04-20

### Реализован stage 4: JavaFX UI поверх существующих `service/storage` слоев

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Этап 4 реализован как отдельный JavaFX-слой поверх уже готовых этапов 1-3. В проект добавлены отдельный UI entry point, главное окно с тремя связанными таблицами <code>Experiment -> Run -> RunResult</code>, формы создания и редактирования, вызов <code>save/load</code> через <code>DataManager</code>, показ summary через сервисный слой и обновленная документация по запуску UI.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\build.gradle`
- `C:\Users\maksi\.codex\Laba1\Stage-4.md`
- `C:\Users\maksi\.codex\Laba1\README.md`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\ExperimentSummary.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\SummaryStats.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\ExperimentSummaryService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\UiLauncher.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\UiMain.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\controller\MainController.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\view\MainView.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\dialog\AlertDialogs.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\dialog\EntityDialogs.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\mapper\UiModelMapper.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\viewmodel\ExperimentRow.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\viewmodel\RunRow.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\ui\viewmodel\RunResultRow.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ExperimentSummaryServiceTest.java`

**Почему сделано именно так**
- JavaFX вынесен в отдельный внешний слой `ui`, а существующие `domain/service/storage/cli` не зависят от него.
- Вместо FXML и набора экранов выбран программный UI с одним главным master-detail окном: так проще контролировать архитектуру и быстрее получить рабочий результат.
- Вариант с "таблицами" реализован через три `TableView`, потому что это естественно отражает предметную область:
  - один `Experiment` -> много `Run`
  - один `Run` -> много `RunResult`
- Логику summary я вынес из CLI в `ExperimentSummaryService`, чтобы JavaFX не дублировал расчеты `count/min/max/avg`.
- `save/load` в UI не общаются напрямую с JSON-DTO, а идут через уже готовый `DataManager`, как и было зафиксировано в плане этапа 4.
- Для таблиц добавлены простые `viewmodel`-классы и `UiModelMapper`, чтобы UI-слой не разрастался форматированием дат, текста деталей и summary.
- Формы создания и редактирования сделаны через JavaFX `Dialog`, а ошибки показываются через отдельный `AlertDialogs`, чтобы контроллер не превращался в смесь layout-кода и alert-логики.
- CLI сохранен как отдельный entry point, а UI запускается через `runUi`, что соответствует требованию не ломать этапы 1-3.

**Финальный код**

#### 1. JavaFX подключен отдельной Gradle-задачей

```groovy
def javafxVersion = '21.0.2'
def javafxPlatform = {
    def osName = System.getProperty('os.name').toLowerCase(Locale.ROOT)
    if (osName.contains('win')) {
        return 'win'
    }
    if (osName.contains('mac')) {
        return 'mac'
    }
    if (osName.contains('linux')) {
        return 'linux'
    }
    throw new GradleException("Unsupported OS for JavaFX: ${System.getProperty('os.name')}")
}.call()

dependencies {
    implementation "org.openjfx:javafx-base:${javafxVersion}:${javafxPlatform}"
    implementation "org.openjfx:javafx-graphics:${javafxVersion}:${javafxPlatform}"
    implementation "org.openjfx:javafx-controls:${javafxVersion}:${javafxPlatform}"
}

tasks.register('runUi', JavaExec) {
    group = 'application'
    description = 'Runs the JavaFX UI'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'ui.UiLauncher'
}
```

Это позволяет:
- не ломать существующий `run` для CLI;
- запускать UI отдельно через `.\gradlew.bat runUi`;
- держать JavaFX как отдельный runtime-слой проекта.

#### 2. Summary вынесен из CLI в сервисный слой

```java
public class ExperimentSummaryService {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;

    public ExperimentSummary summarize(long experimentId) {
        Experiment experiment = experimentService.getById(experimentId);
        Map<MeasurementParam, SummaryStats> statsByParam = new EnumMap<>(MeasurementParam.class);

        for (Run run : runService.listByExperimentId(experimentId)) {
            for (RunResult result : runResultService.listByRunId(run.getId())) {
                statsByParam.compute(result.getParam(), (param, existingStats) ->
                        existingStats == null
                                ? createInitialStats(result.getValue())
                                : mergeStats(existingStats, result.getValue()));
            }
        }

        return new ExperimentSummary(experiment.getId(), experiment.getName(), Map.copyOf(statsByParam));
    }
}
```

```java
public record SummaryStats(int count, double min, double max, double avg) {
}
```

Здесь важно:
- summary больше не живет только внутри `CliRunner`;
- CLI и UI используют один и тот же контракт;
- UI не считает агрегаты сам, а только показывает результат.

#### 3. JavaFX entry point добавлен отдельно от CLI

```java
public final class UiLauncher {
    public static void main(String[] args) {
        Application.launch(UiMain.class, args);
    }
}
```

```java
public class UiMain extends Application {
    @Override
    public void start(Stage stage) {
        ExperimentService experimentService = new ExperimentService();
        RunService runService = new RunService(experimentService);
        RunResultService runResultService = new RunResultService(runService);
        DataManager dataManager = new DataManager(experimentService, runService, runResultService);
        ExperimentSummaryService experimentSummaryService =
                new ExperimentSummaryService(experimentService, runService, runResultService);

        MainController controller = new MainController(
                stage,
                experimentService,
                runService,
                runResultService,
                dataManager,
                experimentSummaryService
        );

        Scene scene = new Scene(controller.createContent(), 1540, 860);
        stage.setScene(scene);
        stage.show();
    }
}
```

Такой вход сохраняет чистое разделение:
- `cli.Main` остается консольным entry point;
- `ui.UiLauncher` и `ui.UiMain` отвечают только за JavaFX.

#### 4. Главное окно собрано как master-detail из трех `TableView`

```java
private final TableView<ExperimentRow> experimentTable = new TableView<>(experimentItems);
private final TableView<RunRow> runTable = new TableView<>(runItems);
private final TableView<RunResultRow> runResultTable = new TableView<>(runResultItems);
private final ComboBox<String> resultFilterComboBox = new ComboBox<>();
private final TextArea detailsArea = new TextArea();
```

```java
HBox center = new HBox(12, experimentPane, runPane, resultPane);
root.setTop(top);
root.setCenter(center);
root.setBottom(bottom);
```

Я выбрал именно такой layout, потому что он сразу показывает связи предметной области:
- слева `Experiment`;
- по центру `Run` выбранного эксперимента;
- справа `RunResult` выбранного прогона;
- внизу детали выбранной сущности.

#### 5. Связь между таблицами централизована в `MainController`

```java
private void configureSelectionListeners() {
    view.experimentTable().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (!updatingSelection) {
            refreshHierarchy(newValue != null ? newValue.id() : null, null, null);
        }
    });

    view.runTable().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (!updatingSelection) {
            refreshHierarchy(selectedExperimentId(), newValue != null ? newValue.id() : null, null);
        }
    });
}
```

```java
private void refreshHierarchy(Long experimentIdToSelect, Long runIdToSelect, Long runResultIdToSelect) {
    updatingSelection = true;
    try {
        List<ExperimentRow> experimentRows = experimentService.list().stream()
                .map(uiModelMapper::toExperimentRow)
                .toList();
        view.experimentItems().setAll(experimentRows);

        ExperimentRow selectedExperimentRow = selectRow(view.experimentTable(), experimentIdToSelect);
        if (selectedExperimentRow == null) {
            view.runItems().clear();
            view.runResultItems().clear();
            return;
        }

        List<RunRow> runRows = runService.listByExperimentId(selectedExperimentRow.id()).stream()
                .map(uiModelMapper::toRunRow)
                .toList();
        view.runItems().setAll(runRows);
        // ...
    } finally {
        updatingSelection = false;
    }

    updateDetailsArea();
}
```

Это самый важный кусок UI-логики stage 4:
- пользователь выбирает строку в одной таблице;
- контроллер сам обновляет зависимые таблицы;
- старые selection корректно сбрасываются;
- после `load` и `refresh` UI перерисовывается целиком через один сценарий.

#### 6. Формы создания и редактирования вынесены в `ui.dialog`

```java
public static Optional<ExperimentFormData> showExperimentDialog(Window owner, Experiment initialValue) {
    Dialog<ExperimentFormData> dialog = createDialog(
            owner,
            initialValue == null ? "New Experiment" : "Edit Experiment",
            initialValue == null ? "Create experiment" : "Update experiment"
    );

    TextField nameField = new TextField(initialValue != null ? initialValue.getName() : "");
    TextArea descriptionArea = new TextArea(initialValue != null && initialValue.getDescription() != null
            ? initialValue.getDescription() : "");
    TextField ownerField = new TextField(initialValue != null ? initialValue.getOwnerUsername() : "");

    Node saveButton = dialog.getDialogPane().lookupButton(getPrimaryActionButton(dialog));
    saveButton.addEventFilter(ActionEvent.ACTION, event -> {
        if (nameField.getText().trim().isEmpty()) {
            AlertDialogs.showError(owner, "Validation error", "Experiment name is required", "Enter experiment name.");
            event.consume();
        }
    });
    // ...
}
```

```java
public static Optional<RunResultFormData> showRunResultDialog(Window owner, RunResult initialValue) {
    ComboBox<MeasurementParam> paramComboBox = new ComboBox<>();
    paramComboBox.getItems().addAll(MeasurementParam.values());

    TextField valueField = new TextField(initialValue != null ? Double.toString(initialValue.getValue()) : "");
    TextField unitField = new TextField(initialValue != null ? initialValue.getUnit() : "");
    TextArea commentArea = new TextArea(initialValue != null && initialValue.getComment() != null
            ? initialValue.getComment() : "");
    // ...
}
```

Почему это важно:
- контроллер не разрастается формами и layout-кодом;
- формы можно переиспользовать и для create, и для edit;
- локальная валидация UI ограничивается только проверкой, что поле не пустое или число парсится, а основная бизнес-валидация всё равно остается в сервисах и домене.

#### 7. UI использует `DataManager` и сервисы, а не лезет в JSON напрямую

```java
private void save() {
    FileChooser fileChooser = createJsonFileChooser("Save data to JSON");
    var file = fileChooser.showSaveDialog(stage);
    if (file == null) {
        return;
    }

    dataManager.saveToFile(file.getAbsolutePath());
    setStatus("Data saved to " + file.getAbsolutePath());
}
```

```java
private void load() {
    FileChooser fileChooser = createJsonFileChooser("Load data from JSON");
    var file = fileChooser.showOpenDialog(stage);
    if (file == null) {
        return;
    }

    dataManager.loadFromFile(file.getAbsolutePath());
    view.resultFilterComboBox().getSelectionModel().select(FILTER_ALL);
    refreshHierarchy(null, null, null);
    setStatus("Data loaded from " + file.getAbsolutePath());
}
```

Это соответствует главному правилу stage 4:
- UI не знает ничего о DTO и `JsonFileStorage`;
- UI вызывает готовый сценарий `save/load`;
- после `load` интерфейс полностью пересобирает отображение данных.

#### 8. README и Stage-4.md обновлены под реальную реализацию

```md
## Запуск JavaFX UI

.\gradlew.bat runUi
```

```md
Stage-4.md уточнен:
- таблицы = `TableView`
- зафиксирован master-detail сценарий
- добавлены минимальные колонки таблиц
- усилено требование вынести summary в сервис
- уточнено поведение selection после create/load/delete
```

#### 9. Тесты дополнены под новый сервисный контракт summary

```java
@Test
void shouldBuildSummaryForExperiment() {
    var summaryService = new ExperimentSummaryService(experimentService, runService, runResultService);
    // ...

    ExperimentSummary summary = summaryService.summarize(experiment.getId());

    SummaryStats phStats = summary.statsByParam().get(MeasurementParam.pH);
    assertEquals(2, phStats.count());
    assertEquals(7.5, phStats.avg());
}
```

Это не UI-тест в прямом смысле, но он фиксирует один из самых важных контрактов, на которые теперь опирается JavaFX.

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

### Закрыт риск stage 3 с `null`-элементами внутри JSON-массивов

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
В <code>FileValidator</code> добавлены явные проверки на <code>null</code>-элементы внутри секций <code>experiments</code>, <code>runs</code>, <code>runResults</code>. Раньше файл вида <code>"experiments": [null]</code> мог привести к <code>NullPointerException</code>, теперь он завершается осмысленным <code>ValidationException</code>.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\storage\persistence\file\FileValidator.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\storage\persistence\file\FileValidatorTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\DataManagerTest.java`

**Почему сделано именно так**
- В плане из `Stage-3.md` было явно зафиксировано, что валидатор не должен падать `NullPointerException` на битом файле.
- Секции JSON уже проверялись на `null`, но элементы внутри списков не проверялись.
- Локальная явная проверка в валидаторе проще и надёжнее, чем надеяться, что Jackson или последующий код всегда дадут аккуратную ошибку сами.

**Финальный код**

```java
private void requireRecord(Object record, String sectionName) {
    if (record == null) {
        throw new ValidationException("Section '" + sectionName + "' contains null item");
    }
}
```

```java
for (ExperimentRecord record : experiments) {
    requireRecord(record, "experiments");
    long id = requirePositive(record.getId(), "Experiment.id");
    // ...
}
```

```java
@Test
void shouldThrowValidationExceptionWhenSnapshotContainsNullItem() throws IOException {
    Files.writeString(file, """
            {
              "experiments": [null],
              "runs": [],
              "runResults": []
            }
            """);

    assertThrows(ValidationException.class, () -> dataManager.loadFromFile(file.toString()));
}
```

---

### Реализован stage 3: JSON storage, restore-контракт, `save/load`, validator и тесты

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Этап 3 собран по плану из <code>Stage-3.md</code> и в логике, согласованной с методичкой: домен получил <code>restore(...)</code>, сервисы стали владеть собственным <code>nextId</code>, добавлены file DTO + mapper + <code>JsonFileStorage</code> + <code>FileValidator</code> + <code>DataManager</code>, а в CLI появились команды <code>save &lt;path&gt;</code> и <code>load &lt;path&gt;</code>.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\Experiment.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\Run.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\RunResult.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\ExperimentService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunResultService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\DataManager.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\storage\persistence\file\*`
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\build.gradle`
- `C:\Users\maksi\.codex\Laba1\src\test\java\domain\*.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\storage\persistence\file\*.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\DataManagerTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\cli\CliPersistenceTest.java`

**Почему сделано именно так**
- Прямую сериализацию доменных объектов в JSON не использовал: файловый формат отделен от домена через DTO и mapper, как и было зафиксировано в `Stage-3.md`.
- `restore(...)` вынесен в доменные классы, чтобы корректно восстанавливать `id`, `createdAt`, `updatedAt` без фальшивого `Instant.now()`.
- Глобальный `IdGenerator` убран из реального контракта stage 3: теперь `nextId` хранится внутри каждого сервиса, и после `load` пересчитывается как `maxId + 1`.
- `FileValidator` валидирует именно file DTO, а не доменные объекты. Это позволяет поймать битый JSON до восстановления сущностей и выдавать понятные `ValidationException`.
- `DataManager.loadFromFile(...)` сначала читает snapshot, валидирует его, собирает временные сервисы и только потом заменяет данные в рабочих сервисах.
- CLI не знает деталей восстановления: команды `save/load` просто делегируют работу в `DataManager`.
- Тесты закрывают именно целевые риски этапа 3: timestamps, round-trip, `nextId`, битый JSON и CLI-сценарии.

**Финальный код**

```java
public static Experiment restore(long id,
                                 String name,
                                 String description,
                                 String ownerUsername,
                                 Instant createdAt,
                                 Instant updatedAt) {
    return new Experiment(id, name, description, ownerUsername, createdAt, updatedAt);
}
```

```java
public void loadRestored(List<Run> restoredRuns) {
    Map<Long, Run> loadedRuns = new TreeMap<>();
    long maxId = 0;

    for (Run run : restoredRuns) {
        experimentService.getById(run.getExperimentId());
        if (loadedRuns.put(run.getId(), run) != null) {
            throw new ValidationException("Duplicate run id: " + run.getId());
        }
        maxId = Math.max(maxId, run.getId());
    }

    runs.clear();
    runs.putAll(loadedRuns);
    nextId = maxId + 1;
}
```

```java
public DataSnapshot toSnapshot(List<Experiment> experiments, List<Run> runs, List<RunResult> runResults) {
    return new DataSnapshot(
            experiments.stream().map(this::toExperimentRecord).toList(),
            runs.stream().map(this::toRunRecord).toList(),
            runResults.stream().map(this::toRunResultRecord).toList()
    );
}
```

```java
public void validate(DataSnapshot snapshot) {
    List<ExperimentRecord> experiments = requireSection(snapshot.getExperiments(), "experiments");
    List<RunRecord> runs = requireSection(snapshot.getRuns(), "runs");
    List<RunResultRecord> runResults = requireSection(snapshot.getRunResults(), "runResults");

    validateExperiments(experiments);
    validateRuns(runs);
    validateRunResults(runResults);
    validateReferences(experiments, runs, runResults);
}
```

```java
public void loadFromFile(String path) throws IOException {
    DataSnapshot snapshot = fileStorage.load(Path.of(path));
    fileValidator.validate(snapshot);

    List<Experiment> restoredExperiments = fileSnapshotMapper.toExperiments(snapshot);
    List<Run> restoredRuns = fileSnapshotMapper.toRuns(snapshot);
    List<RunResult> restoredResults = fileSnapshotMapper.toRunResults(snapshot);

    ExperimentService tempExperimentService = new ExperimentService();
    RunService tempRunService = new RunService(tempExperimentService);
    RunResultService tempRunResultService = new RunResultService(tempRunService);

    tempExperimentService.loadRestored(restoredExperiments);
    tempRunService.loadRestored(restoredRuns);
    tempRunResultService.loadRestored(restoredResults);

    experimentService.loadRestored(tempExperimentService.snapshot());
    runService.loadRestored(tempRunService.snapshot());
    runResultService.loadRestored(tempRunResultService.snapshot());
}
```

```java
private void handleSave(ParsedCommand parsedCommand) throws IOException {
    String path = parseRequiredPathArgument(parsedCommand, "save");
    dataManager.saveToFile(path);
    out.println("Data saved to " + path);
}

private void handleLoad(ParsedCommand parsedCommand) throws IOException {
    String path = parseRequiredPathArgument(parsedCommand, "load");
    dataManager.loadFromFile(path);
    out.println("Data loaded from " + path);
}
```

```java
@Test
void shouldNotReplaceCurrentDataWhenFileIsInvalid() throws IOException {
    assertThrows(ValidationException.class, () -> dataManager.loadFromFile(file.toString()));

    assertEquals(1, experimentService.list().size());
    assertTrue(runService.list().isEmpty());
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-16

### Ревью старта этапа 3 и фиксация плана по ЛР2

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Зафиксированы выводы по ревью начала этапа 3 из <code>pull/2</code> и согласованный план реализации этапов 3 и 4 без конфликта с архитектурой этапов 1 и 2.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\laba-plans.md`

**Почему сделано именно так**
- Нужно коротко сохранить контекст по ЛР2, чтобы дальше не восстанавливать его заново по методичке и PR.
- По ревью видно, что у этапа 3 уже есть каркас под JSON, но еще нет рабочего контракта загрузки, совместимого с текущими `domain/service/cli`.
- Для этапа 4 важно заранее зафиксировать, что JavaFX должен строиться поверх тех же сервисов, а не как отдельная логика.

**Финальный код**
```md
Зафиксировано:
- этап 3 = file storage + load <path> + FileStorage/FileValidator
- этап 4 = JavaFX UI
- в pull/2 есть только заготовка под JSON DTO и частичную валидацию
- главный архитектурный риск: текущий домен не умеет восстанавливать createdAt/updatedAt из файла
- рекомендуемый порядок: restore-контракт -> JSON load -> UI
```

---

## 2026-04-09

### Создание файлов контекста проекта

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Созданы файлы с кратким контекстом проекта и журналом изменений для дальнейшей работы по <code>Laba1</code>.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\AGENTS.md`
- `C:\Users\maksi\.codex\laba-plans.md`

**Почему сделано именно так**
- Проекту нужна короткая оперативная сводка по архитектуре, текущим проблемам и плану работ.
- Нужен отдельный журнал, куда можно складывать уже реализованные изменения без повторного чтения всего репозитория.

**Финальный код**
```md
AGENTS.md содержит:
- путь к проекту и методичку
- рамки этапа 1
- структуру пакетов
- текущее состояние
- известные проблемы
- ближайший план

laba-plans.md содержит:
- все реализованные изменения
- затронутые файлы
- обоснование решения
- итоговый фрагмент кода
```

---

### Анализ проблем сервисного слоя и предложения по исправлению

<div style="padding:12px; border-left:6px solid #b83280; background:#fff5f7;">
<b>Суть изменения</b><br>
Зафиксирован разбор текущих проблем в сервисах проекта и предложения по исправлению перед доработкой этапа 1.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\ExperimentService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunResultService.java`

**Почему сделано именно так**
- По методичке сервисный слой должен владеть коллекциями, генерацией `id`, операциями предметной области и проверками перед сохранением.
- Сейчас сервисы формально есть, но часть логики у них неконсистентна, а часть обязательных проверок отсутствует.
- Если это не исправить, проект легко развалится на вопросах про корректность данных и ответственность слоёв.

**Финальный код**

#### Проблема 1. `id` сервиса не совпадает с `id` сущности

<div style="padding:12px; border-left:6px solid #c53030; background:#fff5f5;">
<b>Что не так</b><br>
Сервис генерирует <code>id</code> как ключ в <code>TreeMap</code>, а сущность генерирует свой собственный <code>id</code> внутри конструктора.
</div>

**Почему это плохо**
- Ломается идентичность объекта.
- `getById(id)` ищет по одному идентификатору, а наружу объект возвращает другой.
- Это очень уязвимое место на защите.

**Что предлагается**
- Генерировать `id` только в сервисах.
- Передавать `id` в конструктор сущности.
- Убрать генерацию `id` из доменных классов.

**Пример реализации**
```java
public final class Experiment {
    private final long id;
    private final Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String description;
    private String ownerUsername;

    public Experiment(long id, String name, String description, String ownerUsername) {
        validateId(id);
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);

        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private static void validateId(long id) {
        if (id <= 0) {
            throw new ValidationException("id должен быть положительным");
        }
    }
}
```

```java
public class ExperimentService {
    private final Map<Long, Experiment> experiments = new TreeMap<>();

    public Experiment add(String name, String description, String ownerUsername) {
        long id = IdGenerator.generateId();
        Experiment experiment = new Experiment(id, name, description, ownerUsername);
        experiments.put(id, experiment);
        return experiment;
    }
}
```

#### Проблема 2. Нет проверки существования связанных сущностей

<div style="padding:12px; border-left:6px solid #dd6b20; background:#fffaf0;">
<b>Что не так</b><br>
<code>RunService</code> не проверяет, существует ли <code>Experiment</code>, а <code>RunResultService</code> не проверяет, существует ли <code>Run</code>.
</div>

**Почему это плохо**
- Можно создать прогон для несуществующего эксперимента.
- Можно создать результат для несуществующего прогона.
- Это нарушает предметную область из методички.

**Что предлагается**
- Внедрить зависимости между сервисами.
- Перед созданием `Run` проверять `Experiment`.
- Перед созданием `RunResult` проверять `Run`.

**Пример реализации**
```java
public class RunService {
    private final Map<Long, Run> runs = new TreeMap<>();
    private final ExperimentService experimentService;

    public RunService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public Run add(long experimentId, String name, String operatorName) {
        experimentService.getById(experimentId);

        long id = IdGenerator.generateId();
        Run run = new Run(id, experimentId, name, operatorName);
        runs.put(id, run);
        return run;
    }
}
```

```java
public class RunResultService {
    private final Map<Long, RunResult> results = new TreeMap<>();
    private final RunService runService;

    public RunResultService(RunService runService) {
        this.runService = runService;
    }

    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
        runService.getById(runId);

        long id = IdGenerator.generateId();
        RunResult result = new RunResult(id, runId, param, value, unit, comment);
        results.put(id, result);
        return result;
    }
}
```

#### Проблема 3. `update(...)` может частично испортить объект

<div style="padding:12px; border-left:6px solid #d69e2e; background:#fffff0;">
<b>Что не так</b><br>
Сервис вызывает несколько сеттеров подряд. Если один сеттер отработал, а следующий выбросил исключение, объект остаётся частично изменённым.
</div>

**Почему это плохо**
- Обновление должно быть атомарным.
- Сейчас объект может остаться в промежуточном и грязном состоянии.

**Что предлагается**
- Сначала валидировать весь набор новых значений.
- Только потом применять изменения.
- Для сложных случаев добавить метод `update(...)` в саму сущность с предварительной полной валидацией.

**Пример реализации**
```java
public final class Experiment {
    public void update(String name, String description, String ownerUsername) {
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);

        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.updatedAt = Instant.now();
    }
}
```

```java
public class ExperimentService {
    public Experiment update(long id, String name, String description, String ownerUsername) {
        Experiment experiment = getById(id);
        experiment.update(name, description, ownerUsername);
        return experiment;
    }
}
```

#### Проблема 4. В сервисах слишком мало предметной логики

<div style="padding:12px; border-left:6px solid #3182ce; background:#ebf8ff;">
<b>Что не так</b><br>
Сейчас в сервисах есть только базовый CRUD, но для вашей предметной области уже нужны выборки по связям и подготовка данных под команды CLI.
</div>

**Почему это плохо**
- На этапе 2 CLI начнёт забирать бизнес-логику на себя.
- Это противоречит методичке: команды должны вызывать методы сервиса, а не копаться в коллекциях сами.

**Что предлагается**
- Добавить выборки по `experimentId` и `runId`.
- Подготовить API под `run_list`, `res_list`, `exp_summary`.

**Пример реализации**
```java
public class RunService {
    private final Map<Long, Run> runs = new TreeMap<>();

    public List<Run> listByExperimentId(long experimentId) {
        return runs.values().stream()
                .filter(run -> run.getExperimentId() == experimentId)
                .toList();
    }
}
```

```java
public class RunResultService {
    private final Map<Long, RunResult> results = new TreeMap<>();

    public List<RunResult> listByRunId(long runId) {
        return results.values().stream()
                .filter(result -> result.getRunId() == runId)
                .toList();
    }
}
```

#### Проблема 5. Ответственность за генерацию `id` размыта

<div style="padding:12px; border-left:6px solid #805ad5; background:#faf5ff;">
<b>Что не так</b><br>
Формально генерация <code>id</code> относится к сервисам, но по факту <code>IdGenerator</code> вызывается и в сервисах, и в доменных классах.
</div>

**Почему это плохо**
- Нарушается граница ответственности.
- Архитектуру сложнее объяснить.
- Труднее доказать, что уникальность `id` реально контролирует слой коллекции.

**Что предлагается**
- Оставить один центр ответственности.
- Для текущей архитектуры самый чистый вариант: `IdGenerator` вызывается только из сервисов.

**Пример реализации**
```java
public final class IdGenerator {
    private static long nextId = 1;

    private IdGenerator() {
    }

    public static long generateId() {
        return nextId++;
    }
}
```

```java
public class RunResultService {
    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
        long id = IdGenerator.generateId();
        RunResult result = new RunResult(id, runId, param, value, unit, comment);
        results.put(id, result);
        return result;
    }
}
```

#### Итоговое предложение по сервисам

<div style="padding:12px; border-left:6px solid #2b6cb0; background:#f7fafc;">
<b>План правок</b><br>
Сначала привести в порядок идентификаторы и ссылочную целостность, затем сделать безопасные обновления и только после этого расширять сервисы под команды CLI.
</div>

```java
ExperimentService experimentService = new ExperimentService();
RunService runService = new RunService(experimentService);
RunResultService runResultService = new RunResultService(runService);
```

```java
// Минимальный целевой контракт сервисного слоя
experimentService.add(...);
experimentService.getById(...);
experimentService.update(...);

runService.add(...);              // с проверкой experimentId
runService.listByExperimentId(...);

runResultService.add(...);        // с проверкой runId
runResultService.listByRunId(...);
```

---

### Реализован рефакторинг сервисов и доменных классов

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Переработан сервисный слой и конструкторы доменных классов: исправлена генерация <code>id</code>, добавлены проверки связей, сделаны атомарные обновления и расширены тесты.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\Experiment.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\Run.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\domain\RunResult.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\ExperimentService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunService.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\service\RunResultService.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ExperimentTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunResultTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ServiceIntegrationTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\util\IdGeneratorTest.java`

**Почему сделано именно так**
- Теперь `id` создаётся только в сервисах, поэтому ключ в коллекции совпадает с `entity.getId()`.
- Сервисы контролируют ссылочную целостность: нельзя создать `Run` без существующего `Experiment` и `RunResult` без существующего `Run`.
- Обновления валидируются целиком до изменения объекта, поэтому нет частичной порчи состояния.
- Сервисный слой получил методы выборки по родительскому `id`, что пригодится для CLI этапа 2.
- Тесты проверяют не только конструкторы, но и реальные инварианты сервисов.

**Финальный код**
```java
public class RunService {
    private final Map<Long, Run> runs = new TreeMap<>();
    private final ExperimentService experimentService;

    public RunService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public Run add(long experimentId, String name, String operatorName) {
        experimentService.getById(experimentId);
        long id = IdGenerator.generateId();
        Run run = new Run(id, experimentId, name, operatorName);
        runs.put(id, run);
        return run;
    }
}
```

```java
public final class RunResult {
    public void update(MeasurementParam param, double value, String unit, String comment) {
        validateParam(param);
        validateValueByParam(param, value);
        validateUnit(unit);
        validateComment(comment);

        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
        this.updatedAt = Instant.now();
    }
}
```

```java
@Test
void shouldThrowWhenRunCreatedForMissingExperiment() {
    var experimentService = new ExperimentService();
    var runService = new RunService(experimentService);

    assertThrows(ValidationException.class, () ->
            runService.add(999L, "run", "operator"));
}
```

```java
./gradlew test
// BUILD SUCCESSFUL
```

---

## 2026-04-10

### Финализация этапа 1: разнесены доменные и сервисные тесты, добавлены заметки к защите

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Тесты разнесены по слоям ответственности: доменные проверки вынесены в пакет <code>domain</code>, сервисные сценарии выделены в отдельные тесты сервисов. Дополнительно создан файл <code>Defense.md</code> с русскими заметками к защите.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\Defense.md`
- `C:\Users\maksi\.codex\Laba1\src\test\java\domain\ExperimentTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\domain\RunTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\domain\RunResultTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ExperimentServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunResultServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ServiceIntegrationTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\util\IdGeneratorTest.java`

**Почему сделано именно так**
- Старые тесты в пакете `service` фактически проверяли доменные конструкторы и сеттеры, из-за чего смешивались уровни ответственности.
- Для этапа 1 на защите удобнее показывать отдельно: доменные инварианты, поведение in-memory сервисов и интеграцию связей между сервисами.
- Такое разбиение упрощает сопровождение: если ломается доменная валидация, падают доменные тесты; если ломается CRUD или проверка связей, падают сервисные тесты.
- `Defense.md` нужен как краткий русскоязычный конспект по архитектурным решениям и аргументам для защиты.

**Финальный код**
```java
package domain;

class ExperimentTest {
    @Test
    void shouldNotPartiallyUpdateExperimentWhenValidationFails() {
        var experiment = new Experiment(1, "old", "desc", "user");

        assertThrows(ValidationException.class, () ->
                experiment.update("", "new desc", "new user"));

        assertEquals("old", experiment.getName());
    }
}
```

```java
package service;

class RunServiceTest {
    @Test
    void shouldAddRunForExistingExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");

        var run = runService.add(experiment.getId(), "run", "operator");

        assertEquals(experiment.getId(), run.getExperimentId());
    }
}
```

```md
Defense.md содержит:
- краткое описание этапа 1
- ключевые архитектурные решения
- аргументы, почему `id` генерируются в сервисах
- аргументы, почему проверки связей живут в сервисах
- тезисы для перехода к CLI этапа 2
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-11

### Завершение основного CLI-набора этапа 2: `run_add`, `run_list`, `run_show`, `res_add`, `res_list`, `exp_summary`

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
После команд по экспериментам в `CliRunner` реализован оставшийся базовый набор CLI по методичке: добавление и просмотр прогонов, добавление и просмотр результатов, а также простая сводка `exp_summary` по параметрам измерений.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\cli\CliRunnerTest.java`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\Defense.md`

**Почему сделано именно так**
- `run_add` и `res_add` сделаны интерактивными, потому что это соответствует методичке и уже существующему шаблону CLI-ввода.
- `run_list`, `run_show`, `res_list` строятся поверх уже готовых сервисных методов `listByExperimentId(...)` и `listByRunId(...)`, без прямого доступа CLI к коллекциям.
- Для `res_list` поддержан только один дополнительный флаг `--param`, чтобы не усложнять синтаксис команды.
- `exp_summary` реализован самым простым способом: результаты собираются по всем прогонам эксперимента, затем значения группируются по `MeasurementParam`, а `count/min/max/avg` считаются обычными циклами.
- Тесты CLI расширены так, чтобы покрывать успешные и ошибочные сценарии для нового набора команд.

**Финальный код**
```java
private void handleRunAdd(ParsedCommand parsedCommand) {
    long experimentId = parseRequiredLongArgument(parsedCommand, "run_add", "experiment id");
    String runName = readRequiredValue("Run name");
    String operatorName = readRequiredValue("Operator");
    Run run = runService.add(experimentId, runName, operatorName);
    out.println("Run created with id " + run.getId());
}
```

```java
private void handleResultList(ParsedCommand parsedCommand) {
    ResultListRequest request = parseResultListRequest(parsedCommand);
    var results = runResultService.listByRunId(request.runId());

    if (request.param() != null) {
        results = results.stream()
                .filter(result -> result.getParam() == request.param())
                .toList();
    }
}
```

```java
private void handleExperimentSummary(ParsedCommand parsedCommand) {
    Map<MeasurementParam, List<Double>> valuesByParam = new EnumMap<>(MeasurementParam.class);
    // значения собираются по всем run -> result, потом по каждой группе считаются count/min/max/avg
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-11

### Расширение CLI: добавлена команда `exp_update`

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
В `CliRunner` добавлена команда <code>exp_update</code>. После упрощения контракта команда обновляет ровно одно поле за вызов в формате <code>exp_update &lt;id&gt; field=value</code>, что проще в коде и легче объясняется на защите.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\cli\CliRunnerTest.java`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`

**Почему сделано именно так**
- По методичке после `exp_show` должна идти команда `exp_update`, поэтому реализация добавлена до перехода к `Run`.
- Сначала был рассмотрен более сложный вариант с несколькими `field=value` и кавычками, но для учебной работы он избыточен и плохо защищается устно.
- Итоговый контракт упрощен до одного `field=value` за команду: это уменьшает сложность парсинга и делает поведение команды очевидным.
- Для обновления одного поля CLI сначала берет текущее состояние объекта, меняет только нужное поле и передает полный набор значений в `experimentService.update(...)`.

**Финальный код**
```java
private void handleExperimentUpdate(ParsedCommand parsedCommand) {
    ExperimentUpdateRequest request = parseExperimentUpdateRequest(parsedCommand);
    Experiment experiment = experimentService.getById(request.id());

    String updatedName = experiment.getName();
    String updatedDescription = experiment.getDescription();
    String updatedOwnerUsername = experiment.getOwnerUsername();

    switch (request.field()) {
        case "name" -> updatedName = request.value();
        case "description" -> updatedDescription = request.value();
        case "ownerUsername" -> updatedOwnerUsername = request.value();
        default -> throw new ValidationException("Unknown experiment field: " + request.field());
    }

    experimentService.update(experiment.getId(), updatedName, updatedDescription, updatedOwnerUsername);
}
```

```java
private ExperimentUpdateRequest parseExperimentUpdateRequest(ParsedCommand parsedCommand) {
    String[] parts = parsedCommand.arguments().split("\\s+");
    long experimentId = Long.parseLong(parts[0]);
    String[] fieldAndValue = parts[1].split("=", 2);
    return new ExperimentUpdateRequest(experimentId, fieldAndValue[0], fieldAndValue[1]);
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-11

### Расширение CLI: добавлена команда `exp_show <id>`

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
После `exp_add` и `exp_list` добавлена команда <code>exp_show &lt;id&gt;</code>, которая показывает подробную информацию об одном эксперименте. Заодно в `CliRunner` появился общий разбор обязательного числового аргумента для команд, которым нужен идентификатор.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\cli\CliRunnerTest.java`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\Defense.md`

**Почему сделано именно так**
- После списка экспериментов логично дать команду просмотра одного объекта по `id`, иначе CLI по экспериментам остается незавершенным.
- `exp_show` принимает ровно один аргумент, поэтому выделен отдельный метод `parseRequiredLongArgument(...)`, чтобы централизованно проверять отсутствие аргумента, лишние аргументы и нечисловой ввод.
- Подробный вывод по одному эксперименту отделен от краткого строкового вывода списка: `exp_list` и `exp_show` решают разные задачи и не должны смешивать форматы.
- Для CLI-теста `exp_show` добавлен тестовый конструктор `CliRunner` с передачей готовых сервисов, чтобы тест не зависел от глобального состояния `IdGenerator` и работал со стабильным `id`.

**Финальный код**
```java
private void handleExperimentShow(ParsedCommand parsedCommand) {
    long experimentId = parseRequiredLongArgument(parsedCommand, "exp_show", "experiment id");
    Experiment experiment = experimentService.getById(experimentId);

    out.println("Experiment details:");
    out.println("Id: " + experiment.getId());
    out.println("Name: " + experiment.getName());
    out.println("Description: " + formatNullableValue(experiment.getDescription()));
    out.println("Owner username: " + experiment.getOwnerUsername());
}
```

```java
private long parseRequiredLongArgument(ParsedCommand parsedCommand, String commandName, String argumentLabel) {
    String arguments = parsedCommand.arguments();
    if (arguments.isEmpty()) {
        throw new ValidationException(commandName + " requires " + argumentLabel);
    }

    String[] parts = arguments.split("\\s+");
    if (parts.length != 1) {
        throw new ValidationException(commandName + " accepts exactly one argument: " + argumentLabel);
    }

    try {
        return Long.parseLong(parts[0]);
    } catch (NumberFormatException e) {
        throw new ValidationException(argumentLabel + " must be a number");
    }
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-11

### Расширение CLI: добавлены разбор аргументов, `exp_add` и `exp_list`

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
`CliRunner` перестал быть только оболочкой для `help` и `exit`: добавлен разбор введенной строки на имя команды и аргументы, реализована первая интерактивная команда <code>exp_add</code> и команда <code>exp_list</code> для вывода созданных экспериментов.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\cli\CliRunnerTest.java`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\Defense.md`

**Почему сделано именно так**
- Для дальнейших CLI-команд нужно было перестать сравнивать всю введенную строку целиком и выделять отдельно имя команды и хвост аргументов.
- `exp_add` выбран первой полезной командой, потому что без возможности создать данные команды просмотра списка почти бессмысленны.
- Интерактивный ввод вынесен в общие методы `readRequiredValue(...)` и `readOptionalValue(...)`, чтобы не дублировать логику для следующих команд.
- `exp_list` оформлен как отдельный обработчик с методом форматирования одной строки, чтобы зафиксировать базовый шаблон для остальных list-команд.
- CLI-тесты идут через подставные потоки ввода-вывода, а не через ручной ввод, чтобы сценарии можно было гонять автоматически в `./gradlew test`.

**Финальный код**
```java
private ParsedCommand parseCommand(String line) {
    String[] parts = line.split("\\s+", 2);
    String name = parts[0].toLowerCase(Locale.ROOT);
    String arguments = parts.length > 1 ? parts[1].trim() : "";
    return new ParsedCommand(name, arguments);
}
```

```java
private void handleExperimentAdd(ParsedCommand parsedCommand) {
    ensureNoArguments(parsedCommand, "exp_add");

    String name = readRequiredValue("Name");
    String description = readOptionalValue("Description");
    String ownerUsername = readRequiredValue("Owner username");

    Experiment experiment = experimentService.add(name, description, ownerUsername);
    out.println("Experiment created with id " + experiment.getId());
}
```

```java
private void handleExperimentList(ParsedCommand parsedCommand) {
    ensureNoArguments(parsedCommand, "exp_list");

    var experiments = experimentService.list();
    if (experiments.isEmpty()) {
        out.println("No experiments found.");
        return;
    }

    out.println("Experiments:");
    for (Experiment experiment : experiments) {
        out.println(formatExperimentLine(experiment));
    }
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-10

### Старт CLI этапа 2: добавлен `CliRunner` и чистый вход через `Main`

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Добавлен стартовый CLI-каркас: отдельный класс <code>CliRunner</code> поднимает сервисы, запускает цикл чтения команд и обрабатывает базовые команды <code>help</code> и <code>exit</code>. Класс <code>Main</code> упрощен до делегирования запуска в <code>CliRunner.run()</code>.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\CliRunner.java`
- `C:\Users\maksi\.codex\Laba1\src\main\java\cli\Main.java`
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`

**Почему сделано именно так**
- Для этапа 2 нужен аккуратный вход в CLI без разрастания `Main`.
- `CliRunner` берет на себя инфраструктурную роль: инициализацию сервисов, чтение строк, диспетчеризацию простых команд и обработку пользовательских ошибок.
- На первом шаге не вводился отдельный `CommandParser`, чтобы не усложнять архитектуру раньше времени: для `help` и `exit` достаточно простого встроенного разбора.
- Такая заготовка позволяет дальше добавлять команды небольшими кусками, не смешивая цикл ввода с бизнес-логикой сервисов.

**Финальный код**
```java
public class Main {
    public static void main(String[] args) {
        CliRunner.run();
    }
}
```

```java
public static void run() {
    new CliRunner().start();
}
```

```java
private void handleCommand(String line) {
    String command = line.toLowerCase(Locale.ROOT);

    switch (command) {
        case "help" -> printHelp();
        case "exit" -> handleExit();
        default -> out.println("Unknown command: " + line + ". Type 'help' to see available commands.");
    }
}
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```

---

## 2026-04-10

### Финальная фиксация этапа 1: обновлен AGENTS.md, перенесены проверки id в сервисные тесты

<div style="padding:12px; border-left:6px solid #2f855a; background:#f0fff4;">
<b>Суть изменения</b><br>
Зафиксировано итоговое состояние этапа 1 в <code>AGENTS.md</code>, обновлены заметки к защите и перенесены проверки генерации <code>id</code> из неудачного <code>IdGeneratorTest</code> в тесты сервисного слоя.
</div>

**Файл или файлы**
- `C:\Users\maksi\.codex\Laba1\AGENTS.md`
- `C:\Users\maksi\.codex\Laba1\Defense.md`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\ExperimentServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\service\RunResultServiceTest.java`
- `C:\Users\maksi\.codex\Laba1\src\test\java\util\IdGeneratorTest.java`

**Почему сделано именно так**
- Отдельный `IdGeneratorTest` не проверял реальный контракт генерации идентификаторов: он сравнивал вручную заданные `id` у доменных объектов, а не поведение сервисов.
- По архитектуре проекта генерация `id` относится к сервисному слою, значит и проверять ее нужно через `ExperimentService`, `RunService` и `RunResultService`.
- `AGENTS.md` нужен как короткая актуальная сводка для следующих сессий работы, а `Defense.md` должен отражать именно текущее состояние тестов и архитектуры.

**Финальный код**
```java
@Test
void shouldGenerateDifferentIdsForDifferentExperiments() {
    var service = new ExperimentService();

    var first = service.add("exp1", "desc1", "user1");
    var second = service.add("exp2", "desc2", "user2");

    assertTrue(first.getId() > 0);
    assertTrue(second.getId() > 0);
    assertTrue(first.getId() != second.getId());
}
```

```java
@Test
void shouldGenerateDifferentIdsForDifferentRuns() {
    var experimentService = new ExperimentService();
    var runService = new RunService(experimentService);
    var experiment = experimentService.add("exp", "desc", "user");

    var first = runService.add(experiment.getId(), "run1", "operator1");
    var second = runService.add(experiment.getId(), "run2", "operator2");

    assertTrue(first.getId() != second.getId());
}
```

```md
AGENTS.md теперь фиксирует:
- текущее состояние этапа 1
- разнесение доменных и сервисных тестов
- перенос проверок `id` в сервисные тесты
- готовность проекта к переходу к CLI этапа 2
```

```bash
./gradlew test
# BUILD SUCCESSFUL
```
