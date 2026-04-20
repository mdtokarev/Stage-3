package service;

import domain.MeasurementParam;
import domain.RunResult;
import validation.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RunResultService {

    private final Map<Long, RunResult> results = new TreeMap<>();
    private final RunService runService;
    private long nextId = 1;

    public RunResultService(RunService runService) {
        this.runService = runService;
    }

    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
        runService.getById(runId);
        long id = nextId++;
        RunResult result = new RunResult(id, runId, param, value, unit, comment);
        results.put(id, result);
        return result;
    }

    public RunResult getById(long id) {
        RunResult result = results.get(id);
        if (result == null) {
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        return result;
    }

    public Collection<RunResult> list() {
        return List.copyOf(results.values());
    }

    public Collection<RunResult> listByRunId(long runId) {
        runService.getById(runId);
        return results.values().stream()
                .filter(result -> result.getRunId() == runId)
                .toList();
    }

    public void remove(long id) {
        if (!results.containsKey(id)) {
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        results.remove(id);
    }

    public RunResult update(long id, MeasurementParam param, double value, String unit, String comment) {
        RunResult result = getById(id);
        result.update(param, value, unit, comment);
        return result;
    }

    public void loadRestored(List<RunResult> restoredResults) {
        Map<Long, RunResult> loadedResults = new TreeMap<>();
        long maxId = 0;

        for (RunResult result : restoredResults) {
            runService.getById(result.getRunId());
            if (loadedResults.put(result.getId(), result) != null) {
                throw new ValidationException("Duplicate run result id: " + result.getId());
            }
            maxId = Math.max(maxId, result.getId());
        }

        results.clear();
        results.putAll(loadedResults);
        nextId = maxId + 1;
    }

    public List<RunResult> snapshot() {
        return new ArrayList<>(results.values());
    }
}
