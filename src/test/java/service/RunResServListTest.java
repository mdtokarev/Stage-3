package service;

import domain.MeasurementParam;
import domain.RunResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RunResServListTest {

    @Test
    void listShouldReturnAllResults() {
        RunResultService service = new RunResultService();

        service.add(1, MeasurementParam.pH, 7.2, "pH", null);
        service.add(1, MeasurementParam.pH, 8.2, "pH", null);

        var list = service.list();
        assertEquals(2, list.size());
    }
}
