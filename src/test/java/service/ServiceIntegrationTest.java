package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceIntegrationTest {

    @Test
    void shouldKeepEntityIdEqualToMapIdWhenAddedThroughService() {
        var experimentService = new ExperimentService();
        var experiment = experimentService.add("exp", "desc", "user");

        assertSame(experiment, experimentService.getById(experiment.getId()));
    }

    @Test
    void shouldThrowWhenRunCreatedForMissingExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);

        assertThrows(ValidationException.class, () ->
                runService.add(999L, "run", "operator"));
    }

    @Test
    void shouldThrowWhenResultCreatedForMissingRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);

        assertThrows(ValidationException.class, () ->
                runResultService.add(999L, MeasurementParam.pH, 7.0, "pH", "comment"));
    }

    @Test
    void shouldNotPartiallyUpdateExperimentWhenValidationFails() {
        var experimentService = new ExperimentService();
        var experiment = experimentService.add("old", "desc", "user");

        assertThrows(ValidationException.class, () ->
                experimentService.update(experiment.getId(), "", "new desc", "new user"));

        assertEquals("old", experiment.getName());
        assertEquals("desc", experiment.getDescription());
        assertEquals("user", experiment.getOwnerUsername());
    }

    @Test
    void shouldListRunsByExperimentId() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);

        var exp1 = experimentService.add("exp1", "desc1", "user1");
        var exp2 = experimentService.add("exp2", "desc2", "user2");
        var run1 = runService.add(exp1.getId(), "run1", "operator1");
        runService.add(exp2.getId(), "run2", "operator2");

        var runs = runService.listByExperimentId(exp1.getId());

        assertEquals(1, runs.size());
        assertTrue(runs.contains(run1));
    }

    @Test
    void shouldListResultsByRunId() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run1 = runService.add(experiment.getId(), "run1", "operator1");
        var run2 = runService.add(experiment.getId(), "run2", "operator2");
        var result1 = runResultService.add(run1.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        runResultService.add(run2.getId(), MeasurementParam.pH, 8.0, "pH", "second");

        var results = runResultService.listByRunId(run1.getId());

        assertEquals(1, results.size());
        assertTrue(results.contains(result1));
    }
}
