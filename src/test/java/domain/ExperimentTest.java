package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExperimentTest {

    @Test
    void shouldCreateExperimentWithValidData() {
        var experiment = new Experiment(1, "name", "desc", "user");

        assertEquals(1, experiment.getId());
        assertEquals("name", experiment.getName());
        assertEquals("desc", experiment.getDescription());
        assertEquals("user", experiment.getOwnerUsername());
    }

    @Test
    void shouldThrowWhenIdIsNotPositive() {
        assertThrows(ValidationException.class, () ->
                new Experiment(0, "name", "desc", "user"));
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        assertThrows(ValidationException.class, () ->
                new Experiment(1, "", "desc", "user"));
    }

    @Test
    void shouldThrowWhenNameTooLong() {
        assertThrows(ValidationException.class, () ->
                new Experiment(1, "a".repeat(129), "desc", "user"));
    }

    @Test
    void shouldThrowWhenDescriptionTooLong() {
        assertThrows(ValidationException.class, () ->
                new Experiment(1, "name", "a".repeat(513), "user"));
    }

    @Test
    void shouldThrowWhenOwnerUsernameIsEmpty() {
        assertThrows(ValidationException.class, () ->
                new Experiment(1, "name", "desc", ""));
    }

    @Test
    void shouldThrowWhenOwnerUsernameTooLong() {
        assertThrows(ValidationException.class, () ->
                new Experiment(1, "name", "desc", "a".repeat(129)));
    }

    @Test
    void shouldThrowWhenSetNameIsEmpty() {
        var experiment = new Experiment(1, "name", "desc", "user");

        assertThrows(ValidationException.class, () -> experiment.setName(""));
    }

    @Test
    void shouldThrowWhenSetOwnerUsernameIsEmpty() {
        var experiment = new Experiment(1, "name", "desc", "user");

        assertThrows(ValidationException.class, () -> experiment.setOwnerUsername(""));
    }

    @Test
    void shouldNotPartiallyUpdateExperimentWhenValidationFails() {
        var experiment = new Experiment(1, "old", "desc", "user");

        assertThrows(ValidationException.class, () ->
                experiment.update("", "new desc", "new user"));

        assertEquals("old", experiment.getName());
        assertEquals("desc", experiment.getDescription());
        assertEquals("user", experiment.getOwnerUsername());
    }

    @Test
    void shouldRestoreExperimentWithGivenTimestamps() {
        Instant createdAt = Instant.parse("2026-04-19T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T11:00:00Z");

        var experiment = Experiment.restore(1, "name", "desc", "user", createdAt, updatedAt);

        assertEquals(createdAt, experiment.getCreatedAt());
        assertEquals(updatedAt, experiment.getUpdatedAt());
    }

    @Test
    void shouldThrowWhenExperimentRestoreHasInvalidTimestampOrder() {
        Instant createdAt = Instant.parse("2026-04-19T11:00:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T10:00:00Z");

        assertThrows(ValidationException.class, () ->
                Experiment.restore(1, "name", "desc", "user", createdAt, updatedAt));
    }
}
