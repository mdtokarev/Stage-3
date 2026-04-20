package storage.persistence.file.dto;

import java.util.List;

public class DataSnapshot {
    private List<ExperimentRecord> experiments;
    private List<RunRecord> runs;
    private List<RunResultRecord> runResults;

    public DataSnapshot() {
    }

    public DataSnapshot(List<ExperimentRecord> experiments, List<RunRecord> runs, List<RunResultRecord> runResults) {
        this.experiments = experiments;
        this.runs = runs;
        this.runResults = runResults;
    }

    public List<ExperimentRecord> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentRecord> experiments) {
        this.experiments = experiments;
    }

    public List<RunRecord> getRuns() {
        return runs;
    }

    public void setRuns(List<RunRecord> runs) {
        this.runs = runs;
    }

    public List<RunResultRecord> getRunResults() {
        return runResults;
    }

    public void setRunResults(List<RunResultRecord> runResults) {
        this.runResults = runResults;
    }
}
