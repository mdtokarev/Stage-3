package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunServiceTest {

    @Test
    void shouldAddRunForExistingExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");

        var run = runService.add(experiment.getId(), "run", "operator");

        assertTrue(run.getId() > 0);
        assertEquals(experiment.getId(), run.getExperimentId());
        assertSame(run, runService.getById(run.getId()));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentRuns() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");

        var first = runService.add(experiment.getId(), "run1", "operator1");
        var second = runService.add(experiment.getId(), "run2", "operator2");

        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > 0);
        assertTrue(first.getId() != second.getId());
    }

    @Test
    void shouldListRuns() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");
        var first = runService.add(experiment.getId(), "run1", "operator1");
        var second = runService.add(experiment.getId(), "run2", "operator2");

        var runs = runService.list();

        assertEquals(2, runs.size());
        assertTrue(runs.contains(first));
        assertTrue(runs.contains(second));
    }

    @Test
    void shouldListRunsByExperimentId() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var firstExperiment = experimentService.add("exp1", "desc1", "user1");
        var secondExperiment = experimentService.add("exp2", "desc2", "user2");
        var expectedRun = runService.add(firstExperiment.getId(), "run1", "operator1");
        runService.add(secondExperiment.getId(), "run2", "operator2");

        var runs = runService.listByExperimentId(firstExperiment.getId());

        assertEquals(1, runs.size());
        assertTrue(runs.contains(expectedRun));
    }

    @Test
    void shouldUpdateRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "old", "operator");

        var updated = runService.update(run.getId(), "new", "new operator");

        assertSame(run, updated);
        assertEquals("new", updated.getName());
        assertEquals("new operator", updated.getOperatorName());
    }

    @Test
    void shouldRemoveRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");

        runService.remove(run.getId());

        assertEquals(0, runService.list().size());
        assertThrows(ValidationException.class, () -> runService.getById(run.getId()));
    }

    @Test
    void shouldThrowWhenAddingRunForMissingExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);

        assertThrows(ValidationException.class, () ->
                runService.add(999L, "run", "operator"));
    }

    @Test
    void shouldThrowWhenRunNotFound() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);

        assertThrows(ValidationException.class, () -> runService.getById(999L));
    }
}
