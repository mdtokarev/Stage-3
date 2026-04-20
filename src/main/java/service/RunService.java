package service;

import domain.Run;
import validation.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RunService {
    private final Map<Long, Run> runs = new TreeMap<>();
    private final ExperimentService experimentService;
    private long nextId = 1;

    public RunService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    public Run add(long experimentId, String name, String operatorName) {
        experimentService.getById(experimentId);
        long id = nextId++;
        Run run = new Run(id, experimentId, name, operatorName);
        runs.put(id, run);
        return run;
    }

    public Run getById(long id) {
        Run run = runs.get(id);
        if (run == null) {
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
        return run;
    }

    public Collection<Run> list() {
        return List.copyOf(runs.values());
    }

    public Collection<Run> listByExperimentId(long experimentId) {
        experimentService.getById(experimentId);
        return runs.values().stream()
                .filter(run -> run.getExperimentId() == experimentId)
                .toList();
    }

    public void remove(long id) {
        if (!runs.containsKey(id)) {
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
        runs.remove(id);
    }

    public Run update(long id, String name, String operatorName) {
        Run run = getById(id);
        run.update(name, operatorName);
        return run;
    }

    public void loadRestored(List<Run> restoredRuns) {
        Map<Long, Run> loadedRuns = new TreeMap<>();
        long maxId = 0;

        for (Run run : restoredRuns) {
            experimentService.getById(run.getExperimentId());
            if (loadedRuns.put(run.getId(), run) != null) {
                throw new ValidationException("Duplicate run id: " + run.getId());
            }
            maxId = Math.max(maxId, run.getId());
        }

        runs.clear();
        runs.putAll(loadedRuns);
        nextId = maxId + 1;
    }

    public List<Run> snapshot() {
        return new ArrayList<>(runs.values());
    }
}
