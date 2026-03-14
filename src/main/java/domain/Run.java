package domain;

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

    public Run(long id, long experimentId, String name, String operatorName, Instant createdAt) {
        this.id = id;
        this.experimentId = experimentId;
        this.name = name;
        this.operatorName = operatorName;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(long experimentId) {
        this.experimentId = experimentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

