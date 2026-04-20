package cli;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import service.ExperimentService;
import service.RunResultService;
import service.RunService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CliRunnerTest {

    @Test
    void shouldAddExperimentViaInteractiveCommand() {
        String input = String.join(System.lineSeparator(),
                "exp_add",
                "Experiment A",
                "Description A",
                "owner_a",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Creating a new experiment."));
        assertTrue(output.contains("Experiment created with id "));
        assertTrue(output.contains("CLI stopped."));
    }

    @Test
    void shouldShowMessageWhenExperimentListIsEmpty() {
        String input = String.join(System.lineSeparator(),
                "exp_list",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("No experiments found."));
    }

    @Test
    void shouldShowAddedExperimentInList() {
        String input = String.join(System.lineSeparator(),
                "exp_add",
                "Experiment A",
                "Description A",
                "owner_a",
                "exp_list",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Experiments:"));
        assertTrue(output.contains("Experiment A"));
        assertTrue(output.contains("owner=owner_a"));
        assertTrue(output.contains("description=Description A"));
    }

    @Test
    void shouldShowExperimentById() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");

        String input = String.join(System.lineSeparator(),
                "exp_show " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Experiment details:"));
        assertTrue(output.contains("Id: " + experiment.getId()));
        assertTrue(output.contains("Name: Experiment A"));
        assertTrue(output.contains("Description: Description A"));
        assertTrue(output.contains("Owner username: owner_a"));
    }

    @Test
    void shouldShowValidationErrorWhenExperimentShowHasNoId() {
        String input = String.join(System.lineSeparator(),
                "exp_show",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: exp_show requires experiment id"));
    }

    @Test
    void shouldShowValidationErrorWhenExperimentShowIdIsNotNumber() {
        String input = String.join(System.lineSeparator(),
                "exp_show abc",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: experiment id must be a number"));
    }

    @Test
    void shouldUpdateExperimentNameAndDescription() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Old name", "Old description", "owner_a");

        String input = String.join(System.lineSeparator(),
                "exp_update " + experiment.getId() + " name=New_name",
                "exp_show " + experiment.getId(),
                "exp_update " + experiment.getId() + " description=Updated_description",
                "exp_show " + experiment.getId(),
                "exp_update " + experiment.getId() + " ownerUsername=owner_b",
                "exp_show " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Experiment updated."));
        assertTrue(output.contains("Name: New_name"));
        assertTrue(output.contains("Description: Updated_description"));
        assertTrue(output.contains("Owner username: owner_b"));
    }

    @Test
    void shouldShowValidationErrorWhenExperimentUpdateHasUnknownField() {
        var experimentService = new ExperimentService();
        var experiment = experimentService.add("Name", "Description", "owner_a");
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);

        String input = String.join(System.lineSeparator(),
                "exp_update " + experiment.getId() + " owner=abc",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: Unknown experiment field: owner"));
    }

    @Test
    void shouldShowValidationErrorWhenExperimentUpdateHasNoFieldValue() {
        var experimentService = new ExperimentService();
        var experiment = experimentService.add("Name", "Description", "owner_a");
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);

        String input = String.join(System.lineSeparator(),
                "exp_update " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: exp_update accepts exactly one field=value"));
    }

    @Test
    void shouldShowValidationErrorWhenExperimentUpdateHasMoreThanOneFieldValue() {
        var experimentService = new ExperimentService();
        var experiment = experimentService.add("Name", "Description", "owner_a");
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);

        String input = String.join(System.lineSeparator(),
                "exp_update " + experiment.getId() + " name=New_name description=Updated_description",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: exp_update accepts exactly one field=value"));
    }

    @Test
    void shouldAddRunViaInteractiveCommand() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");

        String input = String.join(System.lineSeparator(),
                "run_add " + experiment.getId(),
                "Run-2026-02-03-A",
                "yarus",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Creating a new run."));
        assertTrue(output.contains("Run created with id "));
        assertTrue(runService.list().stream().anyMatch(run ->
                run.getExperimentId() == experiment.getId()
                        && run.getName().equals("Run-2026-02-03-A")
                        && run.getOperatorName().equals("yarus")));
    }

    @Test
    void shouldShowValidationErrorWhenRunAddedForMissingExperiment() {
        String input = String.join(System.lineSeparator(),
                "run_add 999999",
                "Run-2026-02-03-A",
                "yarus",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: Experiment not found with id - 999999"));
    }

    @Test
    void shouldShowRunsForExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        runService.add(experiment.getId(), "Run-1", "yarus");
        runService.add(experiment.getId(), "Run-2", "maksi");

        String input = String.join(System.lineSeparator(),
                "run_list " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Runs for experiment " + experiment.getId() + " (Experiment A):"));
        assertTrue(output.contains("Run-1"));
        assertTrue(output.contains("operator=yarus"));
        assertTrue(output.contains("Run-2"));
        assertTrue(output.contains("operator=maksi"));
    }

    @Test
    void shouldShowMessageWhenRunListIsEmpty() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");

        String input = String.join(System.lineSeparator(),
                "run_list " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("No runs found for experiment " + experiment.getId() + "."));
    }

    @Test
    void shouldShowRunById() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        var run = runService.add(experiment.getId(), "Run-1", "yarus");
        runResultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "stable");

        String input = String.join(System.lineSeparator(),
                "run_show " + run.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Run details:"));
        assertTrue(output.contains("Id: " + run.getId()));
        assertTrue(output.contains("Experiment id: " + experiment.getId()));
        assertTrue(output.contains("Name: Run-1"));
        assertTrue(output.contains("Operator: yarus"));
        assertTrue(output.contains("Results: 1"));
    }

    @Test
    void shouldShowValidationErrorWhenRunShowIdIsNotNumber() {
        String input = String.join(System.lineSeparator(),
                "run_show abc",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: run id must be a number"));
    }

    @Test
    void shouldAddResultViaInteractiveCommand() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        var run = runService.add(experiment.getId(), "Run-1", "yarus");

        String input = String.join(System.lineSeparator(),
                "res_add " + run.getId(),
                "pH",
                "7.0",
                "pH",
                "stable",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Creating a new result."));
        assertTrue(output.contains("Result created with id "));
        assertTrue(runResultService.listByRunId(run.getId()).stream().anyMatch(result ->
                result.getParam() == MeasurementParam.pH
                        && result.getValue() == 7.0
                        && result.getUnit().equals("pH")
                        && result.getComment().equals("stable")));
    }

    @Test
    void shouldShowValidationErrorWhenResultAddedForMissingRun() {
        String input = String.join(System.lineSeparator(),
                "res_add 999999",
                "pH",
                "7.0",
                "pH",
                "stable",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: Run with id 999999 doesn't exist"));
    }

    @Test
    void shouldShowResultsForRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        var run = runService.add(experiment.getId(), "Run-1", "yarus");
        runResultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "stable");
        runResultService.add(run.getId(), MeasurementParam.Temperature, 23.5, "C", "room");

        String input = String.join(System.lineSeparator(),
                "res_list " + run.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Results for run " + run.getId() + " (Run-1):"));
        assertTrue(output.contains("value=7.0"));
        assertTrue(output.contains("unit=pH"));
        assertTrue(output.contains("value=23.5"));
        assertTrue(output.contains("unit=C"));
    }

    @Test
    void shouldFilterResultsByParam() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        var run = runService.add(experiment.getId(), "Run-1", "yarus");
        runResultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "stable");
        runResultService.add(run.getId(), MeasurementParam.Temperature, 23.5, "C", "room");

        String input = String.join(System.lineSeparator(),
                "res_list " + run.getId() + " --param pH",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("value=7.0"));
        assertTrue(output.contains("unit=pH"));
        assertTrue(!output.contains("value=23.5"));
    }

    @Test
    void shouldShowValidationErrorWhenResultListRunIdIsNotNumber() {
        String input = String.join(System.lineSeparator(),
                "res_list abc",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Validation error: run id must be a number"));
    }

    @Test
    void shouldShowExperimentSummary() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");
        var run1 = runService.add(experiment.getId(), "Run-1", "yarus");
        var run2 = runService.add(experiment.getId(), "Run-2", "maksi");

        runResultService.add(run1.getId(), MeasurementParam.pH, 7.0, "pH", "stable");
        runResultService.add(run2.getId(), MeasurementParam.pH, 8.0, "pH", "alkaline");
        runResultService.add(run1.getId(), MeasurementParam.Temperature, 20.0, "C", "room");
        runResultService.add(run2.getId(), MeasurementParam.Temperature, 24.0, "C", "warm");

        String input = String.join(System.lineSeparator(),
                "exp_summary " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Summary for experiment " + experiment.getId() + " (Experiment A):"));
        assertTrue(output.contains("pH: count=2 min=7.00 max=8.00 avg=7.50"));
        assertTrue(output.contains("Temperature: count=2 min=20.00 max=24.00 avg=22.00"));
    }

    @Test
    void shouldShowMessageWhenExperimentSummaryHasNoResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var experiment = experimentService.add("Experiment A", "Description A", "owner_a");

        String input = String.join(System.lineSeparator(),
                "exp_summary " + experiment.getId(),
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, experimentService, runService, runResultService).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("No summary data for experiment " + experiment.getId() + "."));
    }
}
