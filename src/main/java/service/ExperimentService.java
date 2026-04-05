package service;

import domain.Experiment;
import util.IdGenerator;
import validation.ExperimentValidator;
import validation.ValidationException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class ExperimentService {

    private final TreeMap<Long, Experiment> experiments = new TreeMap<>(); // коллекция экспериментов
    private final IdGenerator idGenerator = new IdGenerator();

    public Experiment add(String name, String description, String ownerUsername) {
        long id = idGenerator.generateId();

        Experiment exp = new Experiment(name, description, ownerUsername);

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

    public Collection<Experiment> list() {
        return List.copyOf(experiments.values()); // возвращаем копию, чтобы никак нельзя было извне поменять оригинал
    }

    public void remove(long id) {
        if (!experiments.containsKey(id)) {
            throw new ValidationException("Experiment with id - " + id + " doesn't exist");
        }
//        если эксперимента с таким номером НЕТ - кидаем исключение

        experiments.remove(id);
    }

    public Experiment update(long id, String name, String description, String ownerUsername) {
        Experiment exp = getById(id);
//        валидация обновлённых параметров происходит через сеттеры
        exp.setName(name);
        exp.setDescription(description);
        exp.setOwnerUsername(ownerUsername);

        return exp;
    }
}