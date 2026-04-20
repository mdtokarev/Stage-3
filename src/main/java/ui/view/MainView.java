package ui.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;

public class MainView {
    private final ObservableList<ExperimentRow> experimentItems = FXCollections.observableArrayList();
    private final ObservableList<RunRow> runItems = FXCollections.observableArrayList();
    private final ObservableList<RunResultRow> runResultItems = FXCollections.observableArrayList();

    private final BorderPane root = new BorderPane();
    private final TableView<ExperimentRow> experimentTable = new TableView<>(experimentItems);
    private final TableView<RunRow> runTable = new TableView<>(runItems);
    private final TableView<RunResultRow> runResultTable = new TableView<>(runResultItems);
    private final ComboBox<String> resultFilterComboBox = new ComboBox<>();
    private final Button newExperimentButton = new Button("New Experiment");
    private final Button editExperimentButton = new Button("Edit Experiment");
    private final Button showSummaryButton = new Button("Show Summary");
    private final Button newRunButton = new Button("New Run");
    private final Button editRunButton = new Button("Edit Run");
    private final Button newRunResultButton = new Button("New Result");
    private final Button editRunResultButton = new Button("Edit Result");
    private final Button saveButton = new Button("Save");
    private final Button loadButton = new Button("Load");
    private final Button refreshButton = new Button("Refresh");
    private final TextArea detailsArea = new TextArea();
    private final Label statusLabel = new Label("Ready.");

    public MainView() {
        configureTables();
        configureLayout();
    }

    public Parent root() {
        return root;
    }

    public ObservableList<ExperimentRow> experimentItems() {
        return experimentItems;
    }

    public ObservableList<RunRow> runItems() {
        return runItems;
    }

    public ObservableList<RunResultRow> runResultItems() {
        return runResultItems;
    }

    public TableView<ExperimentRow> experimentTable() {
        return experimentTable;
    }

    public TableView<RunRow> runTable() {
        return runTable;
    }

    public TableView<RunResultRow> runResultTable() {
        return runResultTable;
    }

    public ComboBox<String> resultFilterComboBox() {
        return resultFilterComboBox;
    }

    public Button newExperimentButton() {
        return newExperimentButton;
    }

    public Button editExperimentButton() {
        return editExperimentButton;
    }

    public Button showSummaryButton() {
        return showSummaryButton;
    }

    public Button newRunButton() {
        return newRunButton;
    }

    public Button editRunButton() {
        return editRunButton;
    }

    public Button newRunResultButton() {
        return newRunResultButton;
    }

    public Button editRunResultButton() {
        return editRunResultButton;
    }

    public Button saveButton() {
        return saveButton;
    }

    public Button loadButton() {
        return loadButton;
    }

    public Button refreshButton() {
        return refreshButton;
    }

    public TextArea detailsArea() {
        return detailsArea;
    }

    public Label statusLabel() {
        return statusLabel;
    }

    private void configureTables() {
        experimentTable.setPlaceholder(new Label("No experiments."));
        runTable.setPlaceholder(new Label("Select an experiment."));
        runResultTable.setPlaceholder(new Label("Select a run."));
        experimentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        runTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        runResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        experimentTable.getColumns().addAll(
                createNumberColumn("Id", ExperimentRow::id),
                createStringColumn("Name", ExperimentRow::name),
                createStringColumn("Owner", ExperimentRow::ownerUsername),
                createStringColumn("Created", ExperimentRow::createdAt)
        );

        runTable.getColumns().addAll(
                createNumberColumn("Id", RunRow::id),
                createStringColumn("Name", RunRow::name),
                createStringColumn("Operator", RunRow::operatorName),
                createStringColumn("Created", RunRow::createdAt)
        );

        runResultTable.getColumns().addAll(
                createNumberColumn("Id", RunResultRow::id),
                createStringColumn("Param", RunResultRow::param),
                createStringColumn("Value", RunResultRow::value),
                createStringColumn("Unit", RunResultRow::unit),
                createStringColumn("Comment", RunResultRow::comment)
        );

        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefRowCount(8);
        detailsArea.setPromptText("Select an experiment, run or result to see details.");
    }

    private void configureLayout() {
        Label titleLabel = new Label("Laba1 JavaFX UI");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        ToolBar toolBar = new ToolBar(
                newExperimentButton,
                editExperimentButton,
                showSummaryButton,
                new Separator(Orientation.VERTICAL),
                newRunButton,
                editRunButton,
                new Separator(Orientation.VERTICAL),
                newRunResultButton,
                editRunResultButton,
                new Separator(Orientation.VERTICAL),
                saveButton,
                loadButton,
                refreshButton
        );

        VBox top = new VBox(8, titleLabel, toolBar);
        top.setPadding(new Insets(12, 12, 8, 12));

        VBox experimentPane = createTablePane("Experiments", experimentTable);
        VBox runPane = createTablePane("Runs", runTable);

        HBox resultHeader = new HBox(8, new Label("Results"), new Label("Filter:"), resultFilterComboBox);
        VBox resultPane = createTablePane(resultHeader, runResultTable);
        resultFilterComboBox.setPrefWidth(170);

        HBox center = new HBox(12, experimentPane, runPane, resultPane);
        center.setPadding(new Insets(0, 12, 12, 12));
        HBox.setHgrow(experimentPane, Priority.ALWAYS);
        HBox.setHgrow(runPane, Priority.ALWAYS);
        HBox.setHgrow(resultPane, Priority.ALWAYS);

        Label detailsLabel = new Label("Details");
        detailsLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        VBox bottom = new VBox(8, detailsLabel, detailsArea, statusLabel);
        bottom.setPadding(new Insets(0, 12, 12, 12));
        VBox.setVgrow(detailsArea, Priority.ALWAYS);

        root.setTop(top);
        root.setCenter(center);
        root.setBottom(bottom);
    }

    private VBox createTablePane(String title, TableView<?> tableView) {
        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        return createTablePane(label, tableView);
    }

    private VBox createTablePane(javafx.scene.Node header, TableView<?> tableView) {
        VBox box = new VBox(8, header, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return box;
    }

    private <T> TableColumn<T, Number> createNumberColumn(String title, java.util.function.ToLongFunction<T> valueExtractor) {
        TableColumn<T, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(valueExtractor.applyAsLong(cellData.getValue())));
        return column;
    }

    private <T> TableColumn<T, String> createStringColumn(String title, java.util.function.Function<T, String> valueExtractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(valueExtractor.apply(cellData.getValue())));
        return column;
    }
}
