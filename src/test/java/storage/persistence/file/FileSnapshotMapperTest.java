package storage.persistence.file;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.Test;
import storage.persistence.file.dto.DataSnapshot;
import storage.persistence.file.dto.ExperimentRecord;
import storage.persistence.file.dto.RunRecord;
import storage.persistence.file.dto.RunResultRecord;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSnapshotMapperTest {

    private final FileSnapshotMapper mapper = new FileSnapshotMapper();

    @Test
    void shouldMapDomainToSnapshot() {
        var experiment = Experiment.restore(1, "Exp A", "desc", "user",
                Instant.parse("2026-04-19T10:00:00Z"),
                Instant.parse("2026-04-19T10:01:00Z"));
        var run = Run.restore(2, 1, "Run A", "operator",
                Instant.parse("2026-04-19T10:05:00Z"),
                Instant.parse("2026-04-19T10:06:00Z"));
        var result = RunResult.restore(3, 2, MeasurementParam.pH, 7.0, "pH", "ok",
                Instant.parse("2026-04-19T10:10:00Z"),
                Instant.parse("2026-04-19T10:11:00Z"));

        DataSnapshot snapshot = mapper.toSnapshot(List.of(experiment), List.of(run), List.of(result));

        ExperimentRecord experimentRecord = snapshot.getExperiments().get(0);
        RunRecord runRecord = snapshot.getRuns().get(0);
        RunResultRecord resultRecord = snapshot.getRunResults().get(0);

        assertEquals("2026-04-19T10:00:00Z", experimentRecord.getCreatedAt());
        assertEquals("2026-04-19T10:06:00Z", runRecord.getUpdatedAt());
        assertEquals("pH", resultRecord.getParam());
        assertEquals(7.0, resultRecord.getValue());
    }

    @Test
    void shouldRestoreDomainFromSnapshot() {
        DataSnapshot snapshot = new DataSnapshot(
                List.of(new ExperimentRecord(1L, "Exp A", "desc", "user",
                        "2026-04-19T10:00:00Z", "2026-04-19T10:01:00Z")),
                List.of(new RunRecord(2L, 1L, "Run A", "operator",
                        "2026-04-19T10:05:00Z", "2026-04-19T10:06:00Z")),
                List.of(new RunResultRecord(3L, 2L, "pH", 7.0, "pH", "ok",
                        "2026-04-19T10:10:00Z", "2026-04-19T10:11:00Z"))
        );

        Experiment experiment = mapper.toExperiments(snapshot).get(0);
        Run run = mapper.toRuns(snapshot).get(0);
        RunResult result = mapper.toRunResults(snapshot).get(0);

        assertEquals(1L, experiment.getId());
        assertEquals(Instant.parse("2026-04-19T10:01:00Z"), experiment.getUpdatedAt());
        assertEquals(2L, run.getId());
        assertEquals(Instant.parse("2026-04-19T10:05:00Z"), run.getCreatedAt());
        assertEquals(3L, result.getId());
        assertEquals(Instant.parse("2026-04-19T10:11:00Z"), result.getUpdatedAt());
    }
}
