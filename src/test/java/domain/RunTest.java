package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunTest {

    @Test
    void shouldCreateRunWithValidData() {
        var run = new Run(2, 1, "run_name", "run_operator");

        assertEquals(2, run.getId());
        assertEquals(1, run.getExperimentId());
        assertEquals("run_name", run.getName());
        assertEquals("run_operator", run.getOperatorName());
    }

    @Test
    void shouldThrowWhenIdIsNotPositive() {
        assertThrows(ValidationException.class, () ->
                new Run(0, 1, "name", "operator"));
    }

    @Test
    void shouldThrowWhenExperimentIdIsNegative() {
        assertThrows(ValidationException.class, () ->
                new Run(1, -1, "name", "operator"));
    }

    @Test
    void shouldThrowWhenRunNameIsEmpty() {
        assertThrows(ValidationException.class, () ->
                new Run(1, 1, "", "operator"));
    }

    @Test
    void shouldThrowWhenRunNameTooLong() {
        assertThrows(ValidationException.class, () ->
                new Run(1, 1, "a".repeat(129), "operator"));
    }

    @Test
    void shouldThrowWhenOperatorNameIsEmpty() {
        assertThrows(ValidationException.class, () ->
                new Run(1, 1, "name", ""));
    }

    @Test
    void shouldThrowWhenOperatorNameTooLong() {
        assertThrows(ValidationException.class, () ->
                new Run(1, 1, "name", "a".repeat(65)));
    }

    @Test
    void shouldThrowWhenSetNameIsEmpty() {
        var run = new Run(1, 1, "name", "operator");

        assertThrows(ValidationException.class, () -> run.setName(""));
    }

    @Test
    void shouldThrowWhenSetOperatorNameIsEmpty() {
        var run = new Run(1, 1, "name", "operator");

        assertThrows(ValidationException.class, () -> run.setOperatorName(""));
    }

    @Test
    void shouldNotPartiallyUpdateRunWhenValidationFails() {
        var run = new Run(1, 1, "old", "operator");

        assertThrows(ValidationException.class, () ->
                run.update("", "new operator"));

        assertEquals("old", run.getName());
        assertEquals("operator", run.getOperatorName());
    }

    @Test
    void shouldRestoreRunWithGivenTimestamps() {
        Instant createdAt = Instant.parse("2026-04-19T10:05:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T10:15:00Z");

        var run = Run.restore(2, 1, "run_name", "operator", createdAt, updatedAt);

        assertEquals(createdAt, run.getCreatedAt());
        assertEquals(updatedAt, run.getUpdatedAt());
    }

    @Test
    void shouldThrowWhenRunRestoreHasInvalidTimestampOrder() {
        Instant createdAt = Instant.parse("2026-04-19T10:15:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T10:05:00Z");

        assertThrows(ValidationException.class, () ->
                Run.restore(2, 1, "run_name", "operator", createdAt, updatedAt));
    }
}
