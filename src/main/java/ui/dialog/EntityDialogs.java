package ui.dialog;

import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import domain.Experiment;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.util.Optional;

public final class EntityDialogs {
    private EntityDialogs() {
    }

    public static Optional<ExperimentFormData> showExperimentDialog(Window owner, Experiment initialValue) {
        Dialog<ExperimentFormData> dialog = createDialog(
                owner,
                initialValue == null ? "New Experiment" : "Edit Experiment",
                initialValue == null ? "Create experiment" : "Update experiment"
        );

        TextField nameField = new TextField(initialValue != null ? initialValue.getName() : "");
        TextArea descriptionArea = new TextArea(initialValue != null && initialValue.getDescription() != null
                ? initialValue.getDescription() : "");
        descriptionArea.setPrefRowCount(3);
        TextField ownerField = new TextField(initialValue != null ? initialValue.getOwnerUsername() : "");

        GridPane grid = createGridPane();
        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Description"), descriptionArea);
        grid.addRow(2, new Label("Owner username"), ownerField);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(getPrimaryActionButton(dialog));
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                AlertDialogs.showError(owner, "Validation error", "Experiment name is required", "Enter experiment name.");
                event.consume();
            } else if (ownerField.getText().trim().isEmpty()) {
                AlertDialogs.showError(owner, "Validation error", "Owner username is required", "Enter owner username.");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new ExperimentFormData(
                        nameField.getText().trim(),
                        trimToNull(descriptionArea.getText()),
                        ownerField.getText().trim()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<RunFormData> showRunDialog(Window owner, Run initialValue) {
        Dialog<RunFormData> dialog = createDialog(
                owner,
                initialValue == null ? "New Run" : "Edit Run",
                initialValue == null ? "Create run" : "Update run"
        );

        TextField nameField = new TextField(initialValue != null ? initialValue.getName() : "");
        TextField operatorField = new TextField(initialValue != null ? initialValue.getOperatorName() : "");

        GridPane grid = createGridPane();
        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Operator"), operatorField);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(getPrimaryActionButton(dialog));
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                AlertDialogs.showError(owner, "Validation error", "Run name is required", "Enter run name.");
                event.consume();
            } else if (operatorField.getText().trim().isEmpty()) {
                AlertDialogs.showError(owner, "Validation error", "Operator is required", "Enter operator name.");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new RunFormData(nameField.getText().trim(), operatorField.getText().trim());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<RunResultFormData> showRunResultDialog(Window owner, RunResult initialValue) {
        Dialog<RunResultFormData> dialog = createDialog(
                owner,
                initialValue == null ? "New Run Result" : "Edit Run Result",
                initialValue == null ? "Create run result" : "Update run result"
        );

        ComboBox<MeasurementParam> paramComboBox = new ComboBox<>();
        paramComboBox.getItems().addAll(MeasurementParam.values());
        paramComboBox.setValue(initialValue != null ? initialValue.getParam() : MeasurementParam.pH);

        TextField valueField = new TextField(initialValue != null ? Double.toString(initialValue.getValue()) : "");
        TextField unitField = new TextField(initialValue != null ? initialValue.getUnit() : "");
        TextArea commentArea = new TextArea(initialValue != null && initialValue.getComment() != null
                ? initialValue.getComment() : "");
        commentArea.setPrefRowCount(3);

        GridPane grid = createGridPane();
        grid.addRow(0, new Label("Parameter"), paramComboBox);
        grid.addRow(1, new Label("Value"), valueField);
        grid.addRow(2, new Label("Unit"), unitField);
        grid.addRow(3, new Label("Comment"), commentArea);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(getPrimaryActionButton(dialog));
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (paramComboBox.getValue() == null) {
                AlertDialogs.showError(owner, "Validation error", "Measurement parameter is required",
                        "Choose one of the supported parameters.");
                event.consume();
                return;
            }

            if (unitField.getText().trim().isEmpty()) {
                AlertDialogs.showError(owner, "Validation error", "Unit is required", "Enter measurement unit.");
                event.consume();
                return;
            }

            try {
                Double.parseDouble(valueField.getText().trim());
            } catch (NumberFormatException e) {
                AlertDialogs.showError(owner, "Validation error", "Value must be numeric",
                        "Enter a valid number for result value.");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new RunResultFormData(
                        paramComboBox.getValue(),
                        Double.parseDouble(valueField.getText().trim()),
                        unitField.getText().trim(),
                        trimToNull(commentArea.getText())
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static <T> Dialog<T> createDialog(Window owner, String title, String header) {
        Dialog<T> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Save", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );
        return dialog;
    }

    private static ButtonType getPrimaryActionButton(Dialog<?> dialog) {
        return dialog.getDialogPane().getButtonTypes().stream()
                .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst()
                .orElseThrow();
    }

    private static GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(12));
        return gridPane;
    }

    private static String trimToNull(String rawValue) {
        String trimmed = rawValue == null ? "" : rawValue.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ExperimentFormData(String name, String description, String ownerUsername) {
    }

    public record RunFormData(String name, String operatorName) {
    }

    public record RunResultFormData(MeasurementParam param, double value, String unit, String comment) {
    }
}
