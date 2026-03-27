package service;

import domain.Run;
import util.IdGenerator;
import validation.ValidationException;
import validation.RunValidator;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class RunService {
    private final TreeMap<Long, Run> runs = new TreeMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    public Run add(long experimentId, String name, String operatorName) {

        long id = idGenerator.next();

        Run run = new Run(
                id,
                experimentId,
                name,
                operatorName,
                Instant.now()
        );

        RunValidator.validate(run);
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
        return List.copyOf(runs.values()); // возвращаем копию, чтобы никак нельзя было извне поменять оригинал
    }

    public void remove(long id) {
        if (!runs.containsKey(id)) {
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
//        если эксперимента с таким номером НЕТ - кидаем исключение

        runs.remove(id);
    }

    public Run update(long id, String name, String operatorName) {
        Run run = getById(id);

//        Создаём временный объект
        Run temp = new Run(
                run.getId(),
                run.getExperimentId(),
                name,
                operatorName,
                run.getCreatedAt()
        );
//        Валидируем временный объект, и если всё ок - меняем оригинал
        RunValidator.validate(temp);

        run.setName(name);
        run.setOperatorName(operatorName);

        return run;
    }
}