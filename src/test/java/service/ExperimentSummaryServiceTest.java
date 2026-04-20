package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentSummaryServiceTest {

    @Test
    void shouldBuildSummaryForExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var summaryService = new ExperimentSummaryService(experimentService, runService, runResultService);

        var experiment = experimentService.add("Exp A", "desc", "user");
        var run1 = runService.add(experiment.getId(), "Run 1", "operator 1");
        var run2 = runService.add(experiment.getId(), "Run 2", "operator 2");
        runResultService.add(run1.getId(), MeasurementParam.pH, 7.0, "pH", "stable");
        runResultService.add(run2.getId(), MeasurementParam.pH, 8.0, "pH", "alkaline");
        runResultService.add(run1.getId(), MeasurementParam.Temperature, 20.0, "C", "room");
        runResultService.add(run2.getId(), MeasurementParam.Temperature, 24.0, "C", "warm");

        ExperimentSummary summary = summaryService.summarize(experiment.getId());

        SummaryStats phStats = summary.statsByParam().get(MeasurementParam.pH);
        SummaryStats temperatureStats = summary.statsByParam().get(MeasurementParam.Temperature);

        assertEquals(experiment.getId(), summary.experimentId());
        assertEquals("Exp A", summary.experimentName());
        assertEquals(2, phStats.count());
        assertEquals(7.0, phStats.min());
        assertEquals(8.0, phStats.max());
        assertEquals(7.5, phStats.avg());
        assertEquals(2, temperatureStats.count());
        assertEquals(20.0, temperatureStats.min());
        assertEquals(24.0, temperatureStats.max());
        assertEquals(22.0, temperatureStats.avg());
    }

    @Test
    void shouldReturnEmptySummaryWhenExperimentHasNoResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var runResultService = new RunResultService(runService);
        var summaryService = new ExperimentSummaryService(experimentService, runService, runResultService);
        var experiment = experimentService.add("Exp A", "desc", "user");

        ExperimentSummary summary = summaryService.summarize(experiment.getId());

        assertTrue(summary.isEmpty());
    }
}
