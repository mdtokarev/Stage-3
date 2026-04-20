package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentServiceTest {

    @Test
    void shouldAddAndGetExperimentById() {
        var service = new ExperimentService();

        var experiment = service.add("exp", "desc", "user");

        assertTrue(experiment.getId() > 0);
        assertSame(experiment, service.getById(experiment.getId()));
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentExperiments() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");

        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > 0);
        assertTrue(first.getId() != second.getId());
    }

    @Test
    void shouldListAddedExperiments() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");

        var experiments = service.list();

        assertEquals(2, experiments.size());
        assertTrue(experiments.contains(first));
        assertTrue(experiments.contains(second));
    }

    @Test
    void shouldUpdateExperiment() {
        var service = new ExperimentService();
        var experiment = service.add("old", "desc", "user");

        var updated = service.update(experiment.getId(), "new", "new desc", "new user");

        assertSame(experiment, updated);
        assertEquals("new", updated.getName());
        assertEquals("new desc", updated.getDescription());
        assertEquals("new user", updated.getOwnerUsername());
    }

    @Test
    void shouldRemoveExperiment() {
        var service = new ExperimentService();
        var experiment = service.add("exp", "desc", "user");

        service.remove(experiment.getId());

        assertEquals(0, service.list().size());
        assertThrows(ValidationException.class, () -> service.getById(experiment.getId()));
    }

    @Test
    void shouldThrowWhenExperimentNotFound() {
        var service = new ExperimentService();

        assertThrows(ValidationException.class, () -> service.getById(999L));
    }

    @Test
    void shouldThrowWhenRemovingMissingExperiment() {
        var service = new ExperimentService();

        assertThrows(ValidationException.class, () -> service.remove(999L));
    }
}
