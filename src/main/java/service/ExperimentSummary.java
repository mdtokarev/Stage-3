package service;

import domain.MeasurementParam;

import java.util.Map;

public record ExperimentSummary(long experimentId,
                                String experimentName,
                                Map<MeasurementParam, SummaryStats> statsByParam) {

    public boolean isEmpty() {
        return statsByParam.isEmpty();
    }
}
