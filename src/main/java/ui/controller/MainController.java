package ui.controller;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.DataManager;
import service.ExperimentSummary;
import service.ExperimentSummaryService;
import service.ExperimentService;
import service.RunResultService;
import service.RunService;
import ui.dialog.AlertDialogs;
import ui.dialog.EntityDialogs;
import ui.mapper.UiModelMapper;
import ui.view.MainView;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;
import validation.ValidationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainController {
    private static final String FILTER_ALL = "All";

    private final Stage stage;
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;
    private final DataManager dataManager;
    private final ExperimentSummaryService experimentSummaryService;
    private final MainView view = new MainView();
    private final UiModelMapper uiModelMapper = new UiModelMapper();
    private boolean updatingSelection;

    public MainController(Stage stage,
                          ExperimentService experimentService,
                          RunService runService,
                          RunResultService runResultService,
                          DataManager dataManager,
                          ExperimentSummaryService experimentSummaryService) {
        this.stage = stage;
        this.experimentService = experimentService;
        this.runService = runService;
        this.runResultService = runResultService;
        this.dataManager = dataManager;
        this.experimentSummaryService = experimentSummaryService;
    }

    public Parent createContent() {
        configureResultFilter();
        configureSelectionListeners();
        configureActionHandlers();
        configureButtonStateBindings();
        refreshHierarchy(null, null, null);
        setStatus("UI is ready.");
        return view.root();
    }

    private void configureResultFilter() {
        view.resultFilterComboBox().getItems().setAll(FILTER_ALL);
        Arrays.stream(MeasurementParam.values())
                .map(MeasurementParam::name)
                .forEach(view.resultFilterComboBox().getItems()::add);
        view.resultFilterComboBox().getSelectionModel().select(FILTER_ALL);
        view.resultFilterComboBox().setOnAction(event -> {
            Long experimentId = selectedExperimentId();
            Long runId = selectedRunId();
            if (experimentId != null && runId != null) {
                refreshHierarchy(experimentId, runId, null);
            }
        });
    }

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

        view.runResultTable().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingSelection) {
                updateDetailsArea();
            }
        });
    }

    private void configureActionHandlers() {
        view.newExperimentButton().setOnAction(event -> createExperiment());
        view.editExperimentButton().setOnAction(event -> editExperiment());
        view.showSummaryButton().setOnAction(event -> showSummary());
        view.newRunButton().setOnAction(event -> createRun());
        view.editRunButton().setOnAction(event -> editRun());
        view.newRunResultButton().setOnAction(event -> createRunResult());
        view.editRunResultButton().setOnAction(event -> editRunResult());
        view.saveButton().setOnAction(event -> save());
        view.loadButton().setOnAction(event -> load());
        view.refreshButton().setOnAction(event -> refreshHierarchy(selectedExperimentId(), selectedRunId(), selectedRunResultId()));
    }

    private void configureButtonStateBindings() {
        view.editExperimentButton().disableProperty().bind(Bindings.isNull(view.experimentTable().getSelectionModel().selectedItemProperty()));
        view.showSummaryButton().disableProperty().bind(Bindings.isNull(view.experimentTable().getSelectionModel().selectedItemProperty()));
        view.newRunButton().disableProperty().bind(Bindings.isNull(view.experimentTable().getSelectionModel().selectedItemProperty()));
        view.editRunButton().disableProperty().bind(Bindings.isNull(view.runTable().getSelectionModel().selectedItemProperty()));
        view.newRunResultButton().disableProperty().bind(Bindings.isNull(view.runTable().getSelectionModel().selectedItemProperty()));
        view.editRunResultButton().disableProperty().bind(Bindings.isNull(view.runResultTable().getSelectionModel().selectedItemProperty()));
    }

    private void createExperiment() {
        EntityDialogs.showExperimentDialog(stage, null).ifPresent(formData -> executeUiAction(
                "Experiment created with id %d",
                () -> {
                    Experiment experiment = experimentService.add(
                            formData.name(),
                            formData.description(),
                            formData.ownerUsername()
                    );
                    refreshHierarchy(experiment.getId(), null, null);
                    return experiment.getId();
                }
        ));
    }

    private void editExperiment() {
        ExperimentRow selectedRow = view.experimentTable().getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            AlertDialogs.showError(stage, "Selection required", "No experiment selected",
                    "Choose an experiment before editing.");
            return;
        }

        EntityDialogs.showExperimentDialog(stage, selectedRow.source()).ifPresent(formData -> executeUiAction(
                "Experiment %d updated",
                () -> {
                    Experiment experiment = experimentService.update(
                            selectedRow.id(),
                            formData.name(),
                            formData.description(),
                            formData.ownerUsername()
                    );
                    refreshHierarchy(experiment.getId(), selectedRunId(), selectedRunResultId());
                    return experiment.getId();
                }
        ));
    }

    private void createRun() {
        ExperimentRow selectedExperiment = view.experimentTable().getSelectionModel().getSelectedItem();
        if (selectedExperiment == null) {
            AlertDialogs.showError(stage, "Selection required", "No experiment selected",
                    "Choose an experiment before creating a run.");
            return;
        }

        EntityDialogs.showRunDialog(stage, null).ifPresent(formData -> executeUiAction(
                "Run created with id %d",
                () -> {
                    Run run = runService.add(selectedExperiment.id(), formData.name(), formData.operatorName());
                    refreshHierarchy(selectedExperiment.id(), run.getId(), null);
                    return run.getId();
                }
        ));
    }

    private void editRun() {
        RunRow selectedRun = view.runTable().getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            AlertDialogs.showError(stage, "Selection required", "No run selected", "Choose a run before editing.");
            return;
        }

        EntityDialogs.showRunDialog(stage, selectedRun.source()).ifPresent(formData -> executeUiAction(
                "Run %d updated",
                () -> {
                    Run run = runService.update(selectedRun.id(), formData.name(), formData.operatorName());
                    refreshHierarchy(selectedExperimentId(), run.getId(), selectedRunResultId());
                    return run.getId();
                }
        ));
    }

    private void createRunResult() {
        RunRow selectedRun = view.runTable().getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            AlertDialogs.showError(stage, "Selection required", "No run selected",
                    "Choose a run before creating a result.");
            return;
        }

        EntityDialogs.showRunResultDialog(stage, null).ifPresent(formData -> executeUiAction(
                "Run result created with id %d",
                () -> {
                    RunResult runResult = runResultService.add(
                            selectedRun.id(),
                            formData.param(),
                            formData.value(),
                            formData.unit(),
                            formData.comment()
                    );
                    refreshHierarchy(selectedExperimentId(), selectedRun.id(), runResult.getId());
                    return runResult.getId();
                }
        ));
    }

    private void editRunResult() {
        RunResultRow selectedRunResult = view.runResultTable().getSelectionModel().getSelectedItem();
        if (selectedRunResult == null) {
            AlertDialogs.showError(stage, "Selection required", "No result selected",
                    "Choose a result before editing.");
            return;
        }

        EntityDialogs.showRunResultDialog(stage, selectedRunResult.source()).ifPresent(formData -> executeUiAction(
                "Run result %d updated",
                () -> {
                    RunResult runResult = runResultService.update(
                            selectedRunResult.id(),
                            formData.param(),
                            formData.value(),
                            formData.unit(),
                            formData.comment()
                    );
                    refreshHierarchy(selectedExperimentId(), selectedRunId(), runResult.getId());
                    return runResult.getId();
                }
        ));
    }

    private void showSummary() {
        ExperimentRow selectedExperiment = view.experimentTable().getSelectionModel().getSelectedItem();
        if (selectedExperiment == null) {
            AlertDialogs.showError(stage, "Selection required", "No experiment selected",
                    "Choose an experiment before opening summary.");
            return;
        }

        try {
            ExperimentSummary summary = experimentSummaryService.summarize(selectedExperiment.id());
            AlertDialogs.showTextContent(
                    stage,
                    "Experiment Summary",
                    "Summary for experiment " + selectedExperiment.id(),
                    uiModelMapper.formatSummary(summary)
            );
            setStatus("Summary opened for experiment " + selectedExperiment.id() + ".");
        } catch (ValidationException e) {
            showValidationError(e);
        }
    }

    private void save() {
        FileChooser fileChooser = createJsonFileChooser("Save data to JSON");
        var file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            dataManager.saveToFile(file.getAbsolutePath());
            setStatus("Data saved to " + file.getAbsolutePath());
            AlertDialogs.showInfo(stage, "Save completed", "Data saved", file.getAbsolutePath());
        } catch (IOException e) {
            AlertDialogs.showError(stage, "Save failed", "Unable to save data", e.getMessage());
        }
    }

    private void load() {
        FileChooser fileChooser = createJsonFileChooser("Load data from JSON");
        var file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            dataManager.loadFromFile(file.getAbsolutePath());
            view.resultFilterComboBox().getSelectionModel().select(FILTER_ALL);
            refreshHierarchy(null, null, null);
            setStatus("Data loaded from " + file.getAbsolutePath());
            AlertDialogs.showInfo(stage, "Load completed", "Data loaded", file.getAbsolutePath());
        } catch (ValidationException e) {
            showValidationError(e);
        } catch (IOException e) {
            AlertDialogs.showError(stage, "Load failed", "Unable to load data", e.getMessage());
        }
    }

    private FileChooser createJsonFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files", "*.json")
        );
        return fileChooser;
    }

    private void refreshHierarchy(Long experimentIdToSelect, Long runIdToSelect, Long runResultIdToSelect) {
        updatingSelection = true;
        try {
            List<ExperimentRow> experimentRows = experimentService.list().stream()
                    .map(uiModelMapper::toExperimentRow)
                    .toList();
            view.experimentItems().setAll(experimentRows);

            ExperimentRow selectedExperimentRow = selectRow(view.experimentTable(), experimentIdToSelect);
            if (selectedExperimentRow == null) {
                view.experimentTable().getSelectionModel().clearSelection();
                view.runItems().clear();
                view.runResultItems().clear();
                view.runTable().getSelectionModel().clearSelection();
                view.runResultTable().getSelectionModel().clearSelection();
                updateDetailsArea();
                return;
            }

            List<RunRow> runRows = runService.listByExperimentId(selectedExperimentRow.id()).stream()
                    .map(uiModelMapper::toRunRow)
                    .toList();
            view.runItems().setAll(runRows);

            RunRow selectedRunRow = selectRow(view.runTable(), runIdToSelect);
            if (selectedRunRow == null) {
                view.runTable().getSelectionModel().clearSelection();
                view.runResultItems().clear();
                view.runResultTable().getSelectionModel().clearSelection();
                updateDetailsArea();
                return;
            }

            String selectedFilter = view.resultFilterComboBox().getSelectionModel().getSelectedItem();
            List<RunResultRow> runResultRows = runResultService.listByRunId(selectedRunRow.id()).stream()
                    .filter(result -> matchesFilter(result, selectedFilter))
                    .map(uiModelMapper::toRunResultRow)
                    .toList();
            view.runResultItems().setAll(runResultRows);

            RunResultRow selectedRunResultRow = selectRow(view.runResultTable(), runResultIdToSelect);
            if (selectedRunResultRow == null) {
                view.runResultTable().getSelectionModel().clearSelection();
            }
        } finally {
            updatingSelection = false;
        }

        updateDetailsArea();
    }

    private boolean matchesFilter(RunResult runResult, String selectedFilter) {
        return selectedFilter == null
                || Objects.equals(selectedFilter, FILTER_ALL)
                || runResult.getParam().name().equals(selectedFilter);
    }

    private void updateDetailsArea() {
        RunResultRow selectedRunResult = view.runResultTable().getSelectionModel().getSelectedItem();
        if (selectedRunResult != null) {
            view.detailsArea().setText(uiModelMapper.formatRunResultDetails(selectedRunResult.source()));
            return;
        }

        RunRow selectedRun = view.runTable().getSelectionModel().getSelectedItem();
        if (selectedRun != null) {
            view.detailsArea().setText(uiModelMapper.formatRunDetails(selectedRun.source()));
            return;
        }

        ExperimentRow selectedExperiment = view.experimentTable().getSelectionModel().getSelectedItem();
        if (selectedExperiment != null) {
            view.detailsArea().setText(uiModelMapper.formatExperimentDetails(selectedExperiment.source()));
            return;
        }

        view.detailsArea().setText("Select an experiment, run or result to see details.");
    }

    private void showValidationError(ValidationException exception) {
        AlertDialogs.showError(stage, "Validation error", "Operation failed validation", exception.getMessage());
        setStatus("Validation error: " + exception.getMessage());
    }

    private void setStatus(String message) {
        view.statusLabel().setText(message);
    }

    private Long selectedExperimentId() {
        ExperimentRow selectedRow = view.experimentTable().getSelectionModel().getSelectedItem();
        return selectedRow != null ? selectedRow.id() : null;
    }

    private Long selectedRunId() {
        RunRow selectedRow = view.runTable().getSelectionModel().getSelectedItem();
        return selectedRow != null ? selectedRow.id() : null;
    }

    private Long selectedRunResultId() {
        RunResultRow selectedRow = view.runResultTable().getSelectionModel().getSelectedItem();
        return selectedRow != null ? selectedRow.id() : null;
    }

    private <T> T selectRow(javafx.scene.control.TableView<T> tableView, Long entityId) {
        if (entityId == null) {
            return null;
        }

        for (T item : tableView.getItems()) {
            if (matchesId(item, entityId)) {
                tableView.getSelectionModel().select(item);
                tableView.scrollTo(item);
                return item;
            }
        }
        return null;
    }

    private boolean matchesId(Object item, Long entityId) {
        if (item instanceof ExperimentRow experimentRow) {
            return experimentRow.id() == entityId;
        }
        if (item instanceof RunRow runRow) {
            return runRow.id() == entityId;
        }
        if (item instanceof RunResultRow runResultRow) {
            return runResultRow.id() == entityId;
        }
        return false;
    }

    private void executeUiAction(String successTemplate, UiAction action) {
        try {
            long entityId = action.execute();
            setStatus(successTemplate.formatted(entityId));
        } catch (ValidationException e) {
            showValidationError(e);
        }
    }

    @FunctionalInterface
    private interface UiAction {
        long execute();
    }
}
