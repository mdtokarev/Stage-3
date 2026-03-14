package validation;

import domain.Experiment;

public class ExperimentValidator {
    public static void validate(Experiment exp) {

        if (exp.name == null) {
            throw new ValidationException("Experiment name can't be empty");
        }

        if (exp.name.length() >= 128) {
            throw new ValidationException("Experiment name too long.");
        }

        if (exp.description != null && exp.description.length() > 512) {
            throw new ValidationException("Description too long");
        }

        if (exp.ownerUsername == null) {
            throw new ValidationException("Owner username cannot be empty");
        }
    }
}
