package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class ExpServiceUpdateTest {

    @Test
    void shouldUpdateExperiment() {
        ExperimentService service = new ExperimentService();

        var exp = service.add("old", "desc1", "user1");
//  ID остаётся тот же
        var updated = service.update(exp.getId(),"new", "desc2", "user2");

        assertEquals("new", updated.getName());
        assertEquals("desc2", updated.getDescription());
        assertEquals("user2" , updated.getOwnerUsername());
    }

    @Test
    void shouldThrowWhenUpdatingIdDoesNotExist() {
        ExperimentService service = new ExperimentService();

        try {
            service.update(999, "name", "desc", "user");
            fail("Expected exception was not thrown");
        } catch (ValidationException e) {
            // всё ок
        }
    }

    @Test
    void shouldNotUpdateWhenInvalidData() {
        ExperimentService service = new ExperimentService();

        var exp = service.add("valid_name", "desc1", "user1");

        try {
            service.update(exp.getId(), "", "desc2", "user2");
            fail("Expected exception was not thrown");
        } catch (ValidationException e) {
            // всё ок
        }

//  Если update не кинул исключение, то принудительно роняем тест

        var same = service.getById(exp.getId());

        assertEquals("valid_name", same.getName());
        assertEquals("desc1", same.getDescription());
        assertEquals("user1", same.getOwnerUsername());
    }
}
