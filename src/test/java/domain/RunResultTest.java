package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunResultTest {

    @Test
    void shouldCreateRunResultWithValidData() {
        var result = new RunResult(3, 2, MeasurementParam.pH, 7.0, "pH", "comment");

        assertEquals(3, result.getId());
        assertEquals(2, result.getRunId());
        assertEquals(MeasurementParam.pH, result.getParam());
        assertEquals(7.0, result.getValue());
        assertEquals("pH", result.getUnit());
        assertEquals("comment", result.getComment());
    }

    @Test
    void shouldThrowWhenIdIsNotPositive() {
        assertThrows(ValidationException.class, () ->
                new RunResult(0, 1, MeasurementParam.pH, 7.0, "pH", "comment"));
    }

    @Test
    void shouldThrowWhenRunIdIsNegative() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, -1, MeasurementParam.pH, 1, "pH", ""));
    }

    @Test
    void shouldThrowWhenUnitIsEmpty() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, 1, "", ""));
    }

    @Test
    void shouldThrowWhenUnitTooLong() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, 7.0, "a".repeat(17), "comment"));
    }

    @Test
    void shouldThrowWhenCommentTooLong() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "a".repeat(129)));
    }

    @Test
    void shouldThrowWhenSetUnitIsEmpty() {
        var result = new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "comm");

        assertThrows(ValidationException.class, () -> result.setUnit(""));
    }

    @Test
    void shouldThrowWhenSetCommentTooLong() {
        var result = new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "");

        assertThrows(ValidationException.class, () -> result.setComment("a".repeat(129)));
    }

    @Test
    void shouldThrowWhenPhIsNegative() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, -1.0, "pH", "comment"));
    }

    @Test
    void shouldThrowWhenPhIsTooHigh() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, 15.0, "pH", "comment"));
    }

    @Test
    void shouldAllowNegativeTemperature() {
        var result = new RunResult(1, 1, MeasurementParam.Temperature, -10.0, "C", "cold");

        assertEquals(-10.0, result.getValue());
    }

    @Test
    void shouldSetNegativeTemperature() {
        var result = new RunResult(1, 1, MeasurementParam.Temperature, 25.0, "C", "comment");

        result.setValue(-10.0);

        assertEquals(-10.0, result.getValue());
    }

    @Test
    void shouldThrowWhenConcentrationIsNegative() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.Concentration, -1.0, "mg/L", "comment"));
    }

    @Test
    void shouldThrowWhenSetNegativePh() {
        var result = new RunResult(1, 1, MeasurementParam.pH, 7.0, "pH", "comm");

        assertThrows(ValidationException.class, () -> result.setValue(-1));
    }

    @Test
    void shouldThrowWhenSetNegativeConcentration() {
        var result = new RunResult(1, 1, MeasurementParam.Concentration, 50, "g/L", "comm");

        assertThrows(ValidationException.class, () -> result.setValue(-10));
    }

    @Test
    void shouldNotChangeParamWhenOldValueIsInvalidForNewParam() {
        var result = new RunResult(1, 1, MeasurementParam.Temperature, -10.0, "C", "cold");

        assertThrows(ValidationException.class, () -> result.setParam(MeasurementParam.pH));
        assertEquals(MeasurementParam.Temperature, result.getParam());
    }

    @Test
    void shouldNotPartiallyUpdateRunResultWhenValidationFails() {
        var result = new RunResult(1, 1, MeasurementParam.pH, 7.0, "pH", "comment");

        assertThrows(ValidationException.class, () ->
                result.update(MeasurementParam.pH, 20.0, "new", "changed"));

        assertEquals(MeasurementParam.pH, result.getParam());
        assertEquals(7.0, result.getValue());
        assertEquals("pH", result.getUnit());
        assertEquals("comment", result.getComment());
    }

    @Test
    void shouldRestoreRunResultWithGivenTimestamps() {
        Instant createdAt = Instant.parse("2026-04-19T10:10:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T10:20:00Z");

        var result = RunResult.restore(3, 2, MeasurementParam.pH, 7.0, "pH", "comment", createdAt, updatedAt);

        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }

    @Test
    void shouldThrowWhenRunResultRestoreHasInvalidTimestampOrder() {
        Instant createdAt = Instant.parse("2026-04-19T10:20:00Z");
        Instant updatedAt = Instant.parse("2026-04-19T10:10:00Z");

        assertThrows(ValidationException.class, () ->
                RunResult.restore(3, 2, MeasurementParam.pH, 7.0, "pH", "comment", createdAt, updatedAt));
    }
}
