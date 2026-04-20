package storage.persistence.file;

import domain.MeasurementParam;
import storage.persistence.file.dto.DataSnapshot;
import storage.persistence.file.dto.ExperimentRecord;
import storage.persistence.file.dto.RunRecord;
import storage.persistence.file.dto.RunResultRecord;
import validation.ValidationException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileValidator {

    public void validate(DataSnapshot snapshot) {
        if (snapshot == null) {
            throw new ValidationException("Snapshot can't be null");
        }

        List<ExperimentRecord> experiments = requireSection(snapshot.getExperiments(), "experiments");
        List<RunRecord> runs = requireSection(snapshot.getRuns(), "runs");
        List<RunResultRecord> runResults = requireSection(snapshot.getRunResults(), "runResults");

        validateExperiments(experiments);
        validateRuns(runs);
        validateRunResults(runResults);
        validateReferences(experiments, runs, runResults);
    }

    private <T> List<T> requireSection(List<T> section, String sectionName) {
        if (section == null) {
            throw new ValidationException("File is missing '" + sectionName + "' section");
        }
        return section;
    }

    private void validateExperiments(List<ExperimentRecord> experiments) {
        Set<Long> ids = new HashSet<>();
        for (ExperimentRecord record : experiments) {
            requireRecord(record, "experiments");
            long id = requirePositive(record.getId(), "Experiment.id");
            requireUnique(ids, id, "Duplicate experiment id: ");
            requireNonBlank(record.getName(), "Experiment.name can't be empty");
            requireMaxLength(record.getName(), 128, "Experiment.name too long");
            requireMaxLength(record.getDescription(), 512, "Experiment.description too long");
            requireNonBlank(record.getOwnerUsername(), "Experiment.ownerUsername can't be empty");
            requireMaxLength(record.getOwnerUsername(), 128, "Experiment.ownerUsername too long");
            validateTimestamps(record.getCreatedAt(), record.getUpdatedAt(), "Experiment id=" + id);
        }
    }

    private void validateRuns(List<RunRecord> runs) {
        Set<Long> ids = new HashSet<>();
        for (RunRecord record : runs) {
            requireRecord(record, "runs");
            long id = requirePositive(record.getId(), "Run.id");
            requireUnique(ids, id, "Duplicate run id: ");
            requirePositive(record.getExperimentId(), "Run.experimentId");
            requireNonBlank(record.getName(), "Run.name can't be empty");
            requireMaxLength(record.getName(), 128, "Run.name too long");
            requireNonBlank(record.getOperatorName(), "Run.operatorName can't be empty");
            requireMaxLength(record.getOperatorName(), 64, "Run.operatorName too long");
            validateTimestamps(record.getCreatedAt(), record.getUpdatedAt(), "Run id=" + id);
        }
    }

    private void validateRunResults(List<RunResultRecord> runResults) {
        Set<Long> ids = new HashSet<>();
        for (RunResultRecord record : runResults) {
            requireRecord(record, "runResults");
            long id = requirePositive(record.getId(), "RunResult.id");
            requireUnique(ids, id, "Duplicate run result id: ");
            requirePositive(record.getRunId(), "RunResult.runId");
            MeasurementParam param = parseMeasurementParam(record.getParam());
            double value = requireValue(record.getValue(), "RunResult.value is required");
            validateValueByParam(param, value);
            requireNonBlank(record.getUnit(), "RunResult.unit can't be empty");
            requireMaxLength(record.getUnit(), 16, "RunResult.unit too long");
            requireMaxLength(record.getComment(), 128, "RunResult.comment too long");
            validateTimestamps(record.getCreatedAt(), record.getUpdatedAt(), "RunResult id=" + id);
        }
    }

    private void validateReferences(List<ExperimentRecord> experiments,
                                    List<RunRecord> runs,
                                    List<RunResultRecord> runResults) {
        Set<Long> experimentIds = new HashSet<>();
        for (ExperimentRecord record : experiments) {
            requireRecord(record, "experiments");
            experimentIds.add(record.getId());
        }

        Set<Long> runIds = new HashSet<>();
        for (RunRecord record : runs) {
            requireRecord(record, "runs");
            if (!experimentIds.contains(record.getExperimentId())) {
                throw new ValidationException("Run id=" + record.getId()
                        + " references missing experiment id=" + record.getExperimentId());
            }
            runIds.add(record.getId());
        }

        for (RunResultRecord record : runResults) {
            requireRecord(record, "runResults");
            if (!runIds.contains(record.getRunId())) {
                throw new ValidationException("RunResult id=" + record.getId()
                        + " references missing run id=" + record.getRunId());
            }
        }
    }

    private long requirePositive(Long value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
        return value;
    }

    private double requireValue(Double value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private void requireRecord(Object record, String sectionName) {
        if (record == null) {
            throw new ValidationException("Section '" + sectionName + "' contains null item");
        }
    }

    private void requireUnique(Set<Long> ids, long id, String messagePrefix) {
        if (!ids.add(id)) {
            throw new ValidationException(messagePrefix + id);
        }
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    private void requireMaxLength(String value, int maxLength, String message) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(message);
        }
    }

    private MeasurementParam parseMeasurementParam(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException("RunResult.param is required");
        }

        try {
            return MeasurementParam.valueOf(rawValue);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown MeasurementParam: " + rawValue);
        }
    }

    private void validateValueByParam(MeasurementParam param, double value) {
        switch (param) {
            case pH -> {
                if (value < 0 || value > 14) {
                    throw new ValidationException("pH must be between 0 and 14");
                }
            }
            case Concentration -> {
                if (value < 0) {
                    throw new ValidationException("Concentration can't be negative");
                }
            }
            case Temperature -> {
            }
        }
    }

    private void validateTimestamps(String createdAt, String updatedAt, String recordLabel) {
        Instant created = parseInstant(createdAt, recordLabel + " has invalid createdAt");
        Instant updated = parseInstant(updatedAt, recordLabel + " has invalid updatedAt");
        if (updated.isBefore(created)) {
            throw new ValidationException(recordLabel + " has updatedAt before createdAt");
        }
    }

    private Instant parseInstant(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException(message);
        }
    }
}
