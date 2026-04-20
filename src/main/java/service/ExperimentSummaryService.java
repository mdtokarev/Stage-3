package service;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ExperimentSummaryService {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;

    public ExperimentSummaryService(ExperimentService experimentService,
                                    RunService runService,
                                    RunResultService runResultService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.runResultService = runResultService;
    }

    public ExperimentSummary summarize(long experimentId) {
        Experiment experiment = experimentService.getById(experimentId);
        Map<MeasurementParam, SummaryStats> statsByParam = new EnumMap<>(MeasurementParam.class);

        for (Run run : runService.listByExperimentId(experimentId)) {
            for (RunResult result : runResultService.listByRunId(run.getId())) {
                statsByParam.compute(result.getParam(), (param, existingStats) ->
                        existingStats == null
                                ? createInitialStats(result.getValue())
                                : mergeStats(existingStats, result.getValue()));
            }
        }

        return new ExperimentSummary(experiment.getId(), experiment.getName(), Map.copyOf(statsByParam));
    }

    private SummaryStats createInitialStats(double value) {
        return new SummaryStats(1, value, value, value);
    }

    private SummaryStats mergeStats(SummaryStats currentStats, double nextValue) {
        int nextCount = currentStats.count() + 1;
        double nextMin = Math.min(currentStats.min(), nextValue);
        double nextMax = Math.max(currentStats.max(), nextValue);
        double nextAvg = ((currentStats.avg() * currentStats.count()) + nextValue) / nextCount;
        return new SummaryStats(nextCount, nextMin, nextMax, nextAvg);
    }
}
