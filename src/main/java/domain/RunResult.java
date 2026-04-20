package domain;

import validation.ValidationException;

import java.time.Instant;

public final class RunResult {
    private final long id;
    private final long runId;
    private MeasurementParam param;
    private double value;
    private String unit;
    private String comment;
    private final Instant createdAt;
    private Instant updatedAt;

    public RunResult(long id, long runId, MeasurementParam param, double value, String unit, String comment) {
        this(id, runId, param, value, unit, comment, Instant.now(), Instant.now());
    }

    private RunResult(long id,
                      long runId,
                      MeasurementParam param,
                      double value,
                      String unit,
                      String comment,
                      Instant createdAt,
                      Instant updatedAt) {
        validateId(id);
        validateRunId(runId);
        validateParam(param);
        validateValueByParam(param, value);
        validateUnit(unit);
        validateComment(comment);
        validateTimestamps(createdAt, updatedAt);

        this.id = id;
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RunResult restore(long id,
                                    long runId,
                                    MeasurementParam param,
                                    double value,
                                    String unit,
                                    String comment,
                                    Instant createdAt,
                                    Instant updatedAt) {
        return new RunResult(id, runId, param, value, unit, comment, createdAt, updatedAt);
    }

    private static void validateId(long id) {
        if (id <= 0)
            throw new ValidationException("RunResult id must be positive");
    }

    private static void validateRunId(long runId) {
        if (runId <= 0)
            throw new ValidationException("RunID must be positive");
    }

    private static void validateParam(MeasurementParam param) {
        if (param == null)
            throw new ValidationException("Measurement parameter can't be null");
    }

    private static void validateValueByParam(MeasurementParam param, double value) {
        switch (param) {
            case pH:
                if (value < 0)
                    throw new ValidationException("pH can't be negative");
                if (value > 14)
                    throw new ValidationException("pH must be between 0 and 14");
                break;
            case Temperature:
                break;
            case Concentration:
                if (value < 0)
                    throw new ValidationException("Concentration can't be negative");
                break;
            default:
                throw new ValidationException("Unknown measurement parameter - " + param);
        }
    }

    private static void validateUnit(String unit) {
        if (unit == null || unit.isBlank())
            throw new ValidationException("Unit can't be empty");
        if (unit.length() > 16)
            throw new ValidationException("Unit too long");
    }

    private static void validateComment(String comment) {
        if (comment != null && comment.length() > 128)
            throw new ValidationException("Comment too long");
    }

    private static void validateTimestamps(Instant createdAt, Instant updatedAt) {
        if (createdAt == null) {
            throw new ValidationException("RunResult createdAt can't be null");
        }
        if (updatedAt == null) {
            throw new ValidationException("RunResult updatedAt can't be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new ValidationException("RunResult updatedAt can't be before createdAt");
        }
    }

    public void setParam(MeasurementParam param) {
        validateParam(param);
        validateValueByParam(param, this.value);
        this.param = param;
        this.updatedAt = Instant.now();
    }

    public void setValue(double value) {
        validateValueByParam(this.param, value);
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

    public void update(MeasurementParam param, double value, String unit, String comment) {
        validateParam(param);
        validateValueByParam(param, value);
        validateUnit(unit);
        validateComment(comment);

        this.param = param;
        this.value = value;
        this.unit = unit;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
