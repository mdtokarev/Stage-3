package domain;

import util.IdGenerator;
import validation.ValidationException;
import java.time.Instant;

public final class RunResult {
    // Уникальный номер результата. Программа назначает сама.
    private final long id;
    // К какому запуску относится (id запуска).
    // Должен ссылаться на реально существующий Run.
    private final long runId;
    // Что измеряли (PH/CONDUCTIVITY/NITRATE...). Выбирается из списка MeasurementParam.
    private MeasurementParam param;
    // Числовое значение результата.
    private double value;
    // Единицы (например "mg/L"). Нельзя пустое. До 16 символов.
    private String unit;
    // Комментарий (например “after 60 min”). Можно пусто. До 128 символов.
    private String comment;
    // Когда добавили результат. Программа ставит автоматически.
    private final Instant createdAt;
    private Instant updatedAt;

    public RunResult(long runId, MeasurementParam param, double value, String unit, String comment) {
        this.id = IdGenerator.generateId();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        validateRunId(runId);
        validateParam(param);
        validateValue(value);
        validateUnit(unit);
        if (comment != null) validateComment(comment);

        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
    }

    private static void validateRunId(long runId) {
        if (runId <= 0)
            throw new ValidationException("RunID must be positive");
    }

    private static void validateParam(MeasurementParam param) {
        if (param == null)
            throw new ValidationException("Measurement parameter can't be null");
    }

    private static void validateValue(double value) {
        if (value < 0)
            throw new ValidationException("Measurement value can't be negative");
    }

    private static void validateUnit(String unit) {
        if (unit == null || unit.isBlank())
            throw new ValidationException("Unit can't be empty");
    }

    private static void validateComment(String comment) {
        if (comment != null && comment.length() > 128)
            throw new ValidationException("Comment too long");
    }

    public void setParam(MeasurementParam param) {
        validateParam(param);
        this.param = param;
        this.updatedAt = Instant.now();
    }

    public void setValue(double value) {
        validateValue(value);
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public void setUnit(String unit) {
        validateUnit(unit);
        this.unit = unit;
        this.updatedAt = Instant.now();
    }

    public void setComment(String comment) {
        validateComment(comment);
        this.comment = comment;
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }
    public long getRunId() {
        return runId;
    }
    public String getUnit() {
        return unit;
    }
    public MeasurementParam getParam() {
        return param;
    }
    public double getValue() {
        return value;
    }
    public String getComment() {
        return comment;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
