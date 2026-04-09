package util;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
//    Проверяем, что генератор ID генерирует их уникальными
    void shouldGenerateUniqueExpId() {
        var exp1 = new Experiment("name1", "desc1","user1");
        var exp2 = new Experiment("name2", "desc2", "user2");
        var exp3 = new Experiment("name3", "desc3", "user3");

        assertTrue(exp1.getId() != exp2.getId(),
                "Exp_Id_1 = " + exp1.getId() + ", Exp_Id_2 = " + exp2.getId());
        assertTrue(exp1.getId() != exp3.getId(),
                "Exp_Id_1 = " + exp1.getId() + ", Exp_Id_3 = " + exp3.getId());
        assertTrue(exp2.getId() != exp3.getId(),
                "Exp_Id_2 = " + exp2.getId() + ", Exp_Id_3 = " + exp3.getId());
    }

    @Test
    void shouldGenerateUniqueRunId() {
        var run1 = new Run(1, "run1", "operator1");
        var run2 = new Run(2, "run2", "operator2");
        var run3 = new Run(3, "run3", "operator3");

        assertTrue(run1.getId() != run2.getId(),
                "Run_Id_1 = " + run1.getId() + ", Run_Id_2 = " + run2.getId());
        assertTrue(run1.getId() != run3.getId(),
                "Run_Id_1 = " + run1.getId() + ", Run_Id_3 = " + run3.getId());
        assertTrue(run2.getId() != run3.getId(),
                "Run_Id_2 = " + run2.getId() + ", Run_Id_3 = " + run3.getId());
    }

    @Test
    void shouldGenerateUniqueRunResultId(){
        var rr1 = new RunResult(1, MeasurementParam.pH, 7.0, "pH", "comm1");
        var rr2 = new RunResult(2, MeasurementParam.pH, 8.0, "pH", "comm2");
        var rr3 = new RunResult(3, MeasurementParam.pH, 9.0, "pH", "comm3");

        assertTrue(rr1.getId() != rr2.getId(),
                "RunRes_Id_1 = " + rr1.getId() + ", RunRes_Id_2 = " + rr2.getId());
        assertTrue(rr1.getId() != rr3.getId(),
                "RunRes_Id_1 = " + rr1.getId() + ", RunRes_Id_3 = " + rr3.getId());
        assertTrue(rr2.getId() != rr3.getId(),
                "RunRes_Id_2 = " + rr2.getId() + ", RunRes_Id_3 = " + rr3.getId());
    }
}
