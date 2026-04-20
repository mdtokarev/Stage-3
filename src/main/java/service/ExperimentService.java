package service;

import domain.Experiment;
import validation.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExperimentService {

    private final Map<Long, Experiment> experiments = new TreeMap<>();
    private long nextId = 1;

    public Experiment add(String name, String description, String ownerUsername) {
        long id = nextId++;
        Experiment experiment = new Experiment(id, name, description, ownerUsername);
        experiments.put(id, experiment);
        return experiment;
    }

    public Experiment getById(long id) {
        Experiment experiment = experiments.get(id);
        if (experiment == null) {
            throw new ValidationException("Experiment not found with id - " + id);
        }
        return experiment;
    }

    public Collection<Experiment> list() {
        return List.copyOf(experiments.values());
    }

    public void remove(long id) {
        if (!experiments.containsKey(id)) {
            throw new ValidationException("Experiment with id - " + id + " doesn't exist");
        }
        experiments.remove(id);
    }

    public Experiment update(long id, String name, String description, String ownerUsername) {
        Experiment experiment = getById(id);
        experiment.update(name, description, ownerUsername);
        return experiment;
    }

    public void loadRestored(List<Experiment> restoredExperiments) {
        Map<Long, Experiment> loadedExperiments = new TreeMap<>();
        long maxId = 0;

        for (Experiment experiment : restoredExperiments) {
            if (loadedExperiments.put(experiment.getId(), experiment) != null) {
                throw new ValidationException("Duplicate experiment id: " + experiment.getId());
            }
            maxId = Math.max(maxId, experiment.getId());
        }

        experiments.clear();
        experiments.putAll(loadedExperiments);
        nextId = maxId + 1;
    }

    public List<Experiment> snapshot() {
        return new ArrayList<>(experiments.values());
    }
}
