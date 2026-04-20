package storage.persistence.file;

import org.junit.jupiter.api.Test;
import storage.persistence.file.dto.DataSnapshot;
import storage.persistence.file.dto.ExperimentRecord;
import storage.persistence.file.dto.RunRecord;
import storage.persistence.file.dto.RunResultRecord;
import validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    @Test
    void shouldValidateCorrectSnapshot() {
        assertDoesNotThrow(() -> validator.validate(validSnapshot()));
    }

    @Test
    void shouldThrowWhenExperimentsSectionIsMissing() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setExperiments(null);

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenExperimentIdsAreDuplicated() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setExperiments(List.of(
                new ExperimentRecord(1L, "Exp A", "desc", "user", "2026-04-19T10:00:00Z", "2026-04-19T10:00:00Z"),
                new ExperimentRecord(1L, "Exp B", "desc", "user", "2026-04-19T10:01:00Z", "2026-04-19T10:01:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenExperimentsSectionContainsNullItem() {
        DataSnapshot snapshot = validSnapshot();
        List<ExperimentRecord> experiments = new ArrayList<>();
        experiments.add(null);
        snapshot.setExperiments(experiments);

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenRunsSectionContainsNullItem() {
        DataSnapshot snapshot = validSnapshot();
        List<RunRecord> runs = new ArrayList<>();
        runs.add(null);
        snapshot.setRuns(runs);

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenRunResultsSectionContainsNullItem() {
        DataSnapshot snapshot = validSnapshot();
        List<RunResultRecord> runResults = new ArrayList<>();
        runResults.add(null);
        snapshot.setRunResults(runResults);

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenRunReferencesMissingExperiment() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setRuns(List.of(
                new RunRecord(2L, 999L, "Run A", "operator", "2026-04-19T10:05:00Z", "2026-04-19T10:05:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenRunResultParamIsInvalid() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setRunResults(List.of(
                new RunResultRecord(3L, 2L, "InvalidParam", 7.0, "pH", "ok",
                        "2026-04-19T10:10:00Z", "2026-04-19T10:10:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenRunResultDateIsInvalid() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setRunResults(List.of(
                new RunResultRecord(3L, 2L, "pH", 7.0, "pH", "ok",
                        "bad-date", "2026-04-19T10:10:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    void shouldThrowWhenPhValueIsOutOfRange() {
        DataSnapshot snapshot = validSnapshot();
        snapshot.setRunResults(List.of(
                new RunResultRecord(3L, 2L, "pH", 20.0, "pH", "ok",
                        "2026-04-19T10:10:00Z", "2026-04-19T10:10:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    private DataSnapshot validSnapshot() {
        return new DataSnapshot(
                List.of(new ExperimentRecord(1L, "Exp A", "desc", "user",
                        "2026-04-19T10:00:00Z", "2026-04-19T10:00:00Z")),
                List.of(new RunRecord(2L, 1L, "Run A", "operator",
                        "2026-04-19T10:05:00Z", "2026-04-19T10:05:00Z")),
                List.of(new RunResultRecord(3L, 2L, "pH", 7.0, "pH", "ok",
                        "2026-04-19T10:10:00Z", "2026-04-19T10:10:00Z"))
        );
    }
}
