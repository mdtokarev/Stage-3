package ui.mapper;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import service.ExperimentSummary;
import service.SummaryStats;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

public class UiModelMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public ExperimentRow toExperimentRow(Experiment experiment) {
        return new ExperimentRow(
                experiment,
                experiment.getId(),
                experiment.getName(),
                experiment.getOwnerUsername(),
                formatInstant(experiment.getCreatedAt())
        );
    }

    public RunRow toRunRow(Run run) {
        return new RunRow(
                run,
                run.getId(),
                run.getName(),
                run.getOperatorName(),
                formatInstant(run.getCreatedAt())
        );
    }

    public RunResultRow toRunResultRow(RunResult runResult) {
        return new RunResultRow(
                runResult,
                runResult.getId(),
                runResult.getParam().name(),
                formatDecimal(runResult.getValue()),
                runResult.getUnit(),
                formatNullable(runResult.getComment())
        );
    }

    public String formatExperimentDetails(Experiment experiment) {
        return """
                Experiment
                Id: %d
                Name: %s
                Description: %s
                Owner username: %s
                Created at: %s
                Updated at: %s
                """.formatted(
                experiment.getId(),
                experiment.getName(),
                formatNullable(experiment.getDescription()),
                experiment.getOwnerUsername(),
                formatInstant(experiment.getCreatedAt()),
                formatInstant(experiment.getUpdatedAt())
        );
    }

    public String formatRunDetails(Run run) {
        return """
                Run
                Id: %d
                Experiment id: %d
                Name: %s
                Operator: %s
                Created at: %s
                Updated at: %s
                """.formatted(
                run.getId(),
                run.getExperimentId(),
                run.getName(),
                run.getOperatorName(),
                formatInstant(run.getCreatedAt()),
                formatInstant(run.getUpdatedAt())
        );
    }

    public String formatRunResultDetails(RunResult runResult) {
        return """
                Run result
                Id: %d
                Run id: %d
                Parameter: %s
                Value: %s
                Unit: %s
                Comment: %s
                Created at: %s
                Updated at: %s
                """.formatted(
                runResult.getId(),
                runResult.getRunId(),
                runResult.getParam(),
                formatDecimal(runResult.getValue()),
                runResult.getUnit(),
                formatNullable(runResult.getComment()),
                formatInstant(runResult.getCreatedAt()),
                formatInstant(runResult.getUpdatedAt())
        );
    }

    public String formatSummary(ExperimentSummary summary) {
        if (summary.isEmpty()) {
            return "No summary data for experiment %d.".formatted(summary.experimentId());
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Summary for experiment ")
                .append(summary.experimentId())
                .append(" (")
                .append(summary.experimentName())
                .append("):")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        summary.statsByParam().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().ordinal()))
                .forEach(entry -> appendSummaryLine(builder, entry.getKey(), entry.getValue()));

        return builder.toString().trim();
    }

    private void appendSummaryLine(StringBuilder builder, MeasurementParam param, SummaryStats stats) {
        builder.append(param)
                .append(": count=")
                .append(stats.count())
                .append(" min=")
                .append(formatDecimal(stats.min()))
                .append(" max=")
                .append(formatDecimal(stats.max()))
                .append(" avg=")
                .append(formatDecimal(stats.avg()))
                .append(System.lineSeparator());
    }

    private String formatInstant(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant);
    }

    private String formatNullable(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
