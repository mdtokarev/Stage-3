package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;
import static org.junit.jupiter.api.Assertions.*;

public class RunServiceUpdateTest {

    @Test
    void shouldUpdateRun() {
        RunService service = new RunService();

        var run = service.add(1, "run1", "operator1");
        var updated = service.update(run.getId(), "run2", "operator2");

        assertEquals("run2", updated.getName());
        assertEquals("operator2", updated.getOperatorName());
    }

    @Test
    void shouldThrowWhenUpdatingIdDoesNotExist() {
        RunService service = new RunService();

        try {
            service.update(999, "name", "operator");
            fail("Expected exception was not thrown");
        } catch (ValidationException e) {
            // ок
        }
    }

    @Test
    void shouldNotUpdateWhenInvalidData() {
        RunService service = new RunService();

        var run = service.add(1, "valid_name", "operator");

        try {
            service.update(run.getId(), "", "operator2");
            fail("Expected exception was not thrown");
        } catch (ValidationException e) {
            // ок
        }

        var same = service.getById(run.getId());
        assertEquals("valid_name", same.getName());
    }
}
