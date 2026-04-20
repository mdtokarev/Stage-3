package service;

import domain.Experiment;
import domain.Run;
import domain.RunResult;
import storage.persistence.file.FileValidator;
import storage.persistence.file.FileSnapshotMapper;
import storage.persistence.file.JsonFileStorage;
import storage.persistence.file.dto.DataSnapshot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DataManager {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;
    private final JsonFileStorage fileStorage;
    private final FileValidator fileValidator;
    private final FileSnapshotMapper fileSnapshotMapper;

    public DataManager(ExperimentService experimentService,
                       RunService runService,
                       RunResultService runResultService) {
        this(
                experimentService,
                runService,
                runResultService,
                new JsonFileStorage(),
                new FileValidator(),
                new FileSnapshotMapper()
        );
    }

    DataManager(ExperimentService experimentService,
                RunService runService,
                RunResultService runResultService,
                JsonFileStorage fileStorage,
                FileValidator fileValidator,
                FileSnapshotMapper fileSnapshotMapper) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.runResultService = runResultService;
        this.fileStorage = fileStorage;
        this.fileValidator = fileValidator;
        this.fileSnapshotMapper = fileSnapshotMapper;
    }

    public void saveToFile(String path) throws IOException {
        DataSnapshot snapshot = fileSnapshotMapper.toSnapshot(
                experimentService.snapshot(),
                runService.snapshot(),
                runResultService.snapshot()
        );
        fileStorage.save(Path.of(path), snapshot);
    }

    public void loadFromFile(String path) throws IOException {
        DataSnapshot snapshot = fileStorage.load(Path.of(path));
        fileValidator.validate(snapshot);

        List<Experiment> restoredExperiments = fileSnapshotMapper.toExperiments(snapshot);
        List<Run> restoredRuns = fileSnapshotMapper.toRuns(snapshot);
        List<RunResult> restoredResults = fileSnapshotMapper.toRunResults(snapshot);

        ExperimentService tempExperimentService = new ExperimentService();
        RunService tempRunService = new RunService(tempExperimentService);
        RunResultService tempRunResultService = new RunResultService(tempRunService);

        tempExperimentService.loadRestored(restoredExperiments);
        tempRunService.loadRestored(restoredRuns);
        tempRunResultService.loadRestored(restoredResults);

        experimentService.loadRestored(tempExperimentService.snapshot());
        runService.loadRestored(tempRunService.snapshot());
        runResultService.loadRestored(tempRunResultService.snapshot());
    }
}
