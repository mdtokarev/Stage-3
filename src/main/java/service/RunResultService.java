package service;

import domain.MeasurementParam;
import domain.RunResult;
import util.IdGenerator;
import validation.ValidationException;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class RunResultService {

    private final TreeMap<Long, RunResult> results = new TreeMap<>();

    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
        long id = IdGenerator.generateId();

        RunResult result = new RunResult(runId, param, value, unit, comment);

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

    public void remove(long id) {
        if (!results.containsKey(id)) {
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        results.remove(id);
    }

    public RunResult update(long id, MeasurementParam param, double value, String unit, String comment) {
        RunResult result = getById(id);

        result.setParam(param);
        result.setValue(value);
        result.setUnit(unit);
        result.setComment(comment);

        return result;
    }
}
