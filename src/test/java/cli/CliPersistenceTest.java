package cli;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.ExperimentService;
import service.RunResultService;
import service.RunService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CliPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveViaCli() throws Exception {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("Exp A", "desc", "user");
        var run = runService.add(experiment.getId(), "Run A", "operator");
        resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");

        Path file = tempDir.resolve("cli-save.json");
        String input = String.join(System.lineSeparator(),
                "save " + file,
                "exit"
        ) + System.lineSeparator();

        String output = runCli(input, experimentService, runService, resultService);

        assertTrue(output.contains("Data saved to " + file));
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("\"experiments\""));
    }

    @Test
    void shouldLoadViaCli() throws Exception {
        Path file = tempDir.resolve("cli-load.json");
        Files.writeString(file, """
                {
                  "experiments": [
                    {
                      "id": 1,
                      "name": "Exp A",
                      "description": "desc",
                      "ownerUsername": "user",
                      "createdAt": "2026-04-19T10:00:00Z",
                      "updatedAt": "2026-04-19T10:00:00Z"
                    }
                  ],
                  "runs": [
                    {
                      "id": 1,
                      "experimentId": 1,
                      "name": "Run A",
                      "operatorName": "operator",
                      "createdAt": "2026-04-19T10:05:00Z",
                      "updatedAt": "2026-04-19T10:05:00Z"
                    }
                  ],
                  "runResults": [
                    {
                      "id": 1,
                      "runId": 1,
                      "param": "pH",
                      "value": 7.0,
                      "unit": "pH",
                      "comment": "ok",
                      "createdAt": "2026-04-19T10:10:00Z",
                      "updatedAt": "2026-04-19T10:10:00Z"
                    }
                  ]
                }
                """);

        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        String input = String.join(System.lineSeparator(),
                "load " + file,
                "exp_list",
                "run_list 1",
                "res_list 1",
                "exit"
        ) + System.lineSeparator();

        String output = runCli(input, experimentService, runService, resultService);

        assertTrue(output.contains("Data loaded from " + file));
        assertTrue(output.contains("Exp A"));
        assertTrue(output.contains("Run A"));
        assertTrue(output.contains("value=7.0"));
    }

    @Test
    void shouldShowValidationErrorWhenCliLoadGetsBrokenJson() throws Exception {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, """
                {
                  "experiments": [],
                  "runs": [
                    {
                      "id": 1,
                      "experimentId": 999,
                      "name": "Broken run",
                      "operatorName": "operator",
                      "createdAt": "2026-04-19T10:05:00Z",
                      "updatedAt": "2026-04-19T10:05:00Z"
                    }
                  ],
                  "runResults": []
                }
                """);

        String input = String.join(System.lineSeparator(),
                "load " + file,
                "exit"
        ) + System.lineSeparator();

        String output = runCli(input, new ExperimentService(), new RunService(new ExperimentService()), null);

        assertTrue(output.contains("Validation error:"));
    }

    private String runCli(String input,
                          ExperimentService experimentService,
                          RunService runService,
                          RunResultService resultService) throws Exception {
        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
        var actualRunService = runService != null ? runService : new RunService(experimentService);
        var actualResultService = resultService != null ? resultService : new RunResultService(actualRunService);

        new CliRunner(inputStream, out, experimentService, actualRunService, actualResultService).start();
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
