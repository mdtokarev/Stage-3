package domain;
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

    public RunResult(long id, long runId, MeasurementParam param, double value, String unit, String comment, Instant createdAt) {
        this.id = id;
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getRunId() {
        return runId;
    }

    public MeasurementParam getParam() {
        return param;
    }

    public void setParam(MeasurementParam param) {
        this.param = param;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
