package service;

import domain.Experiment;
import util.IdGenerator;
import validation.ExperimentValidator;
import validation.ValidationException;

import java.util.TreeMap;

public class ExperimentService {

    private final TreeMap<Long, Experiment> experiments = new TreeMap<>(); // коллекция экспериментов
    private final IdGenerator idGenerator = new IdGenerator();

    public Experiment add(String name, String description, String ownerUsername) {

        long id = idGenerator.next();

        Experiment exp = new Experiment(
                id,
                name,
                description,
                ownerUsername,
                java.time.Instant.now(),
                java.time.Instant.now()
        );

        ExperimentValidator.validate(exp);

        experiments.put(id, exp);
        return exp;
    }

    public Experiment getById(long id) {
        Experiment exp = experiments.get(id);

        if (exp == null) {
            throw new ValidationException("Experiment not found with id - " + id);
        }

        return exp;
    }
}