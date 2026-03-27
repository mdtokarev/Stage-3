package validation;

import domain.Run;

public class RunValidator {
    public static void validate(Run run) {
        if (run.getExperimentId() <= 0) {
            throw new ValidationException("Invalid experimentId");
        }

        if (run.getName() == null || run.getName().isBlank()) {
            throw new ValidationException("Run name cannot be empty");
        }

        if (run.getName().length() > 128) {
            throw new ValidationException("Run name too long");
        }

        if (run.getOperatorName() == null) {
            throw new ValidationException("Operator name cannot be empty");
        }

        if (run.getOperatorName().length() > 64) {
            throw new ValidationException("Operator name too long");
        }
    }
}
