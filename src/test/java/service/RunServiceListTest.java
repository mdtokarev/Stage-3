package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RunServiceListTest {
    @Test
    void listShouldReturnAllRuns() {

        RunService service = new RunService();

        service.add(1, "run1", "operator");
        service.add(1, "run2", "operator");

        var list = service.list();

        assertEquals(2, list.size());
    }
}
