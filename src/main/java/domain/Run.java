package domain;

import util.IdGenerator;
import validation.ValidationException;
import java.time.Instant;

public final class Run {
    // Уникальный номер запуска. Программа назначает сама.
    private final long id;
    // К какому эксперименту относится (id эксперимента).
    // Должен ссылаться на реально существующий Experiment.
    private long experimentId;
    // Название запуска reminder: “Run-2026-02-03-A”. Нельзя пустое. До 128 символов.
    private String name;
    // Кто выполнял запуск (логин или имя). Нельзя пустое. До 64 символов.
    private String operatorName;
    // Когда запуск зарегистрирован. Программа ставит автоматически.
    private final Instant createdAt;
    private Instant updatedAt;

    public Run(long experimentId, String name, String operatorName) {
        this.id = IdGenerator.generateId();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        validateExperimentId(experimentId);
        validateName(name);
        validateOperatorName(operatorName);

        this.experimentId = experimentId;
        this.name = name;
        this.operatorName = operatorName;
    }

    private static void validateExperimentId(long experimentId) {
        if (experimentId <= 0)
            throw new ValidationException("Experiment ID must be positive");
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Run name can't be empty");
        if (name.length() > 128)
            throw new ValidationException("Run name too long");
    }

    private static void validateOperatorName(String operatorName) {
        if (operatorName == null || operatorName.isBlank())
            throw new ValidationException("Operator Name can't be empty");
        if (operatorName.length() > 64)
            throw new ValidationException("Operator name too long");
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setOperatorName(String operatorName) {
        validateOperatorName(operatorName);
        this.operatorName = operatorName;
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public long getExperimentId() {
        return experimentId;
    }
    public String getOperatorName() {
        return operatorName;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}

