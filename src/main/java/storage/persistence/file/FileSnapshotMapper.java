package storage.persistence.file;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import storage.persistence.file.dto.DataSnapshot;
import storage.persistence.file.dto.ExperimentRecord;
import storage.persistence.file.dto.RunRecord;
import storage.persistence.file.dto.RunResultRecord;

import java.time.Instant;
import java.util.List;

public class FileSnapshotMapper {

    public DataSnapshot toSnapshot(List<Experiment> experiments, List<Run> runs, List<RunResult> runResults) {
        return new DataSnapshot(
                experiments.stream().map(this::toExperimentRecord).toList(),
                runs.stream().map(this::toRunRecord).toList(),
                runResults.stream().map(this::toRunResultRecord).toList()
        );
    }

    public List<Experiment> toExperiments(DataSnapshot snapshot) {
        return snapshot.getExperiments().stream()
                .map(this::toExperiment)
                .toList();
    }

    public List<Run> toRuns(DataSnapshot snapshot) {
        return snapshot.getRuns().stream()
                .map(this::toRun)
                .toList();
    }

    public List<RunResult> toRunResults(DataSnapshot snapshot) {
        return snapshot.getRunResults().stream()
                .map(this::toRunResult)
                .toList();
    }

    private ExperimentRecord toExperimentRecord(Experiment experiment) {
        return new ExperimentRecord(
                experiment.getId(),
                experiment.getName(),
                experiment.getDescription(),
                experiment.getOwnerUsername(),
                experiment.getCreatedAt().toString(),
                experiment.getUpdatedAt().toString()
        );
    }

    private RunRecord toRunRecord(Run run) {
        return new RunRecord(
                run.getId(),
                run.getExperimentId(),
                run.getName(),
                run.getOperatorName(),
                run.getCreatedAt().toString(),
                run.getUpdatedAt().toString()
        );
    }

    private RunResultRecord toRunResultRecord(RunResult runResult) {
        return new RunResultRecord(
                runResult.getId(),
                runResult.getRunId(),
                runResult.getParam().name(),
                runResult.getValue(),
                runResult.getUnit(),
                runResult.getComment(),
                runResult.getCreatedAt().toString(),
                runResult.getUpdatedAt().toString()
        );
    }

    private Experiment toExperiment(ExperimentRecord record) {
        return Experiment.restore(
                record.getId(),
                record.getName(),
                record.getDescription(),
                record.getOwnerUsername(),
                Instant.parse(record.getCreatedAt()),
                Instant.parse(record.getUpdatedAt())
        );
    }

    private Run toRun(RunRecord record) {
        return Run.restore(
                record.getId(),
                record.getExperimentId(),
                record.getName(),
                record.getOperatorName(),
                Instant.parse(record.getCreatedAt()),
                Instant.parse(record.getUpdatedAt())
        );
    }

    private RunResult toRunResult(RunResultRecord record) {
        return RunResult.restore(
                record.getId(),
                record.getRunId(),
                MeasurementParam.valueOf(record.getParam()),
                record.getValue(),
                record.getUnit(),
                record.getComment(),
                Instant.parse(record.getCreatedAt()),
                Instant.parse(record.getUpdatedAt())
        );
    }
}
