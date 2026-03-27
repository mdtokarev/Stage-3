package validation;

import domain.Experiment;

public class ExperimentValidator {
    public static void validate(Experiment exp) {

        if (exp.getName() == null || exp.getName().isBlank()) {
            throw new ValidationException("Experiment name can't be empty");
        }
//        isBlank - дает true, если строка пустая или состоит из пробелов, табуляций и тд

        if (exp.getName().length() >= 128) {
            throw new ValidationException("Experiment name too long");
        }

        if (exp.getDescription() != null && exp.getDescription().length() > 512) {
            throw new ValidationException("Description too long");
        }

        if (exp.getOwnerUsername() == null) {
            throw new ValidationException("Owner username cannot be empty");
        }
    }
}
