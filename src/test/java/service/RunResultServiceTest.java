package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunResultServiceTest {

    @Test
    void shouldAddResultForExistingRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");

        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        assertTrue(result.getId() > 0);
        assertEquals(run.getId(), result.getRunId());
        assertSame(result, resultService.getById(result.getId()));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");

        var first = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        var second = resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", "second");

        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > 0);
        assertTrue(first.getId() != second.getId());
    }

    @Test
    void shouldListResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var first = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        var second = resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", "second");

        var results = resultService.list();

        assertEquals(2, results.size());
        assertTrue(results.contains(first));
        assertTrue(results.contains(second));
    }

    @Test
    void shouldListResultsByRunId() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var firstRun = runService.add(experiment.getId(), "run1", "operator1");
        var secondRun = runService.add(experiment.getId(), "run2", "operator2");
        var expectedResult = resultService.add(firstRun.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        resultService.add(secondRun.getId(), MeasurementParam.pH, 8.0, "pH", "second");

        var results = resultService.listByRunId(firstRun.getId());

        assertEquals(1, results.size());
        assertTrue(results.contains(expectedResult));
    }

    @Test
    void shouldUpdateResult() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        var updated = resultService.update(result.getId(), MeasurementParam.Temperature, 25.0, "C", "updated");

        assertSame(result, updated);
        assertEquals(MeasurementParam.Temperature, updated.getParam());
        assertEquals(25.0, updated.getValue());
        assertEquals("C", updated.getUnit());
        assertEquals("updated", updated.getComment());
    }

    @Test
    void shouldRemoveResult() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        resultService.remove(result.getId());

        assertEquals(0, resultService.list().size());
        assertThrows(ValidationException.class, () -> resultService.getById(result.getId()));
    }

    @Test
    void shouldThrowWhenAddingResultForMissingRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        assertThrows(ValidationException.class, () ->
                resultService.add(999L, MeasurementParam.pH, 7.0, "pH", "comment"));
    }

    @Test
    void shouldThrowWhenResultNotFound() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        assertThrows(ValidationException.class, () -> resultService.getById(999L));
    }
}
