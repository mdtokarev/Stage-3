package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import validation.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadRoundTrip() throws IOException {
        var sourceExperimentService = new ExperimentService();
        var sourceRunService = new RunService(sourceExperimentService);
        var sourceResultService = new RunResultService(sourceRunService);
        var sourceDataManager = new DataManager(sourceExperimentService, sourceRunService, sourceResultService);

        var experiment = sourceExperimentService.add("Exp A", "desc", "user");
        var run = sourceRunService.add(experiment.getId(), "Run A", "operator");
        sourceResultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");

        Path file = tempDir.resolve("snapshot.json");
        sourceDataManager.saveToFile(file.toString());

        var targetExperimentService = new ExperimentService();
        var targetRunService = new RunService(targetExperimentService);
        var targetResultService = new RunResultService(targetRunService);
        var targetDataManager = new DataManager(targetExperimentService, targetRunService, targetResultService);

        targetDataManager.loadFromFile(file.toString());

        assertEquals(1, targetExperimentService.list().size());
        assertEquals(1, targetRunService.list().size());
        assertEquals(1, targetResultService.list().size());
        assertEquals("Exp A", targetExperimentService.getById(experiment.getId()).getName());
        assertEquals("Run A", targetRunService.getById(run.getId()).getName());
        assertEquals(MeasurementParam.pH, targetResultService.list().stream().findFirst().orElseThrow().getParam());
    }

    @Test
    void shouldContinueIdsAfterLoad(@TempDir Path dir) throws IOException {
        var sourceExperimentService = new ExperimentService();
        var sourceRunService = new RunService(sourceExperimentService);
        var sourceResultService = new RunResultService(sourceRunService);
        var sourceDataManager = new DataManager(sourceExperimentService, sourceRunService, sourceResultService);

        var experiment = sourceExperimentService.add("Exp A", "desc", "user");
        var run = sourceRunService.add(experiment.getId(), "Run A", "operator");
        sourceResultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");

        Path file = dir.resolve("snapshot.json");
        sourceDataManager.saveToFile(file.toString());

        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var dataManager = new DataManager(experimentService, runService, resultService);

        dataManager.loadFromFile(file.toString());

        var nextExperiment = experimentService.add("Exp B", null, "user2");
        var nextRun = runService.add(run.getExperimentId(), "Run B", "operator2");
        var nextResult = resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", null);

        assertEquals(experiment.getId() + 1, nextExperiment.getId());
        assertEquals(run.getId() + 1, nextRun.getId());
        assertEquals(2L, nextResult.getId());
    }

    @Test
    void shouldNotReplaceCurrentDataWhenFileIsInvalid() throws IOException {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var dataManager = new DataManager(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp A", "desc", "user");
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, """
                {
                  "experiments": [],
                  "runs": [
                    {
                      "id": 1,
                      "experimentId": 999,
                      "name": "Broken run",
                      "operatorName": "op",
                      "createdAt": "2026-04-19T10:05:00Z",
                      "updatedAt": "2026-04-19T10:05:00Z"
                    }
                  ],
                  "runResults": []
                }
                """);

        assertThrows(ValidationException.class, () -> dataManager.loadFromFile(file.toString()));

        assertEquals(1, experimentService.list().size());
        assertEquals("Exp A", experimentService.getById(experiment.getId()).getName());
        assertTrue(runService.list().isEmpty());
        assertTrue(resultService.list().isEmpty());
    }

    @Test
    void shouldLoadEmptySnapshot() throws IOException {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var dataManager = new DataManager(experimentService, runService, resultService);

        Path file = tempDir.resolve("empty.json");
        Files.writeString(file, """
                {
                  "experiments": [],
                  "runs": [],
                  "runResults": []
                }
                """);

        dataManager.loadFromFile(file.toString());

        assertTrue(experimentService.list().isEmpty());
        assertTrue(runService.list().isEmpty());
        assertTrue(resultService.list().isEmpty());
    }

    @Test
    void shouldThrowValidationExceptionWhenSnapshotContainsNullItem() throws IOException {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var dataManager = new DataManager(experimentService, runService, resultService);

        Path file = tempDir.resolve("null-item.json");
        Files.writeString(file, """
                {
                  "experiments": [null],
                  "runs": [],
                  "runResults": []
                }
                """);

        assertThrows(ValidationException.class, () -> dataManager.loadFromFile(file.toString()));
    }
}
