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

//        Создаём временный объект
        Experiment temp = new Experiment(
                exp.getId(),
                name,
                description,
                ownerUsername,
                exp.getCreatedAt(),
                Instant.now()
        );
//        Валидируем временный объект, и если всё ок - меняем оригинал
        ExperimentValidator.validate(temp);

        exp.setName(name);
        exp.setDescription(description);
        exp.setOwnerUsername(ownerUsername);
        exp.setUpdatedAt(Instant.now());

        return exp;
    }
    /*Если не создавать временный объект, то
    * мы сначала меняем оригинал, и только
    * потом валидируем его. Из-за этого
    * ложился тест. Мы не трогаем и не
    * меняем оригинал, пока не убедимся,
    * что всё ок.*/
}