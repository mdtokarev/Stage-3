package cli;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import service.DataManager;
import service.ExperimentSummary;
import service.ExperimentSummaryService;
import service.ExperimentService;
import service.RunResultService;
import service.RunService;
import service.SummaryStats;
import validation.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class CliRunner {

    private final Scanner scanner;
    private final PrintStream out;
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;
    private final DataManager dataManager;
    private final ExperimentSummaryService experimentSummaryService;
    private boolean running;

    public CliRunner() {
        this(System.in, System.out);
    }

    public CliRunner(InputStream inputStream, PrintStream out) {
        this(inputStream, out, new ExperimentService(), null, null);
    }

    CliRunner(InputStream inputStream,
              PrintStream out,
              ExperimentService experimentService,
              RunService runService,
              RunResultService runResultService) {
        this.out = out;
        this.scanner = new Scanner(inputStream);
        this.experimentService = experimentService;
        this.runService = runService != null ? runService : new RunService(experimentService);
        this.runResultService = runResultService != null ? runResultService : new RunResultService(this.runService);
        this.dataManager = new DataManager(this.experimentService, this.runService, this.runResultService);
        this.experimentSummaryService = new ExperimentSummaryService(this.experimentService, this.runService, this.runResultService);
    }

    public static void run() {
        new CliRunner().start();
    }

    void start() {
        running = true;
        printWelcome();

        while (running && scanner.hasNextLine()) {
            out.print("> ");
            out.flush();
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            handleCommand(line);
        }
    }

    private void handleCommand(String line) {
        ParsedCommand parsedCommand = parseCommand(line);

        try {
            switch (parsedCommand.name()) {
                case "help" -> printHelp();
                case "exit" -> handleExit();
                case "save" -> handleSave(parsedCommand);
                case "load" -> handleLoad(parsedCommand);
                case "exp_add" -> handleExperimentAdd(parsedCommand);
                case "exp_list" -> handleExperimentList(parsedCommand);
                case "exp_show" -> handleExperimentShow(parsedCommand);
                case "exp_update" -> handleExperimentUpdate(parsedCommand);
                case "exp_summary" -> handleExperimentSummary(parsedCommand);
                case "run_add" -> handleRunAdd(parsedCommand);
                case "run_list" -> handleRunList(parsedCommand);
                case "run_show" -> handleRunShow(parsedCommand);
                case "res_add" -> handleResultAdd(parsedCommand);
                case "res_list" -> handleResultList(parsedCommand);
                default -> out.println("Unknown command: " + line + ". Type 'help' to see available commands.");
            }
        } catch (ValidationException e) {
            out.println("Validation error: " + e.getMessage());
        } catch (IOException e) {
            out.println("I/O error: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("Unexpected error: " + e.getMessage());
        }
    }

    private ParsedCommand parseCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        String name = parts[0].toLowerCase(Locale.ROOT);
        String arguments = parts.length > 1 ? parts[1].trim() : "";
        return new ParsedCommand(name, arguments);
    }

    private void printWelcome() {
        out.println("Experiment CLI started.");
        out.println("Type 'help' to see available commands.");
    }

    private void printHelp() {
        out.println("Available commands:");
        out.println("help - show available commands");
        out.println("save <path> - save data to JSON file");
        out.println("load <path> - load data from JSON file");
        out.println("exp_add - create a new experiment");
        out.println("exp_list - show all experiments");
        out.println("exp_show <id> - show one experiment");
        out.println("exp_update <id> field=value ... - update experiment");
        out.println("exp_summary <id> - show summary for experiment");
        out.println("run_add <experimentId> - create a run for experiment");
        out.println("run_list <experimentId> - show runs for experiment");
        out.println("run_show <runId> - show one run");
        out.println("res_add <runId> - add a result for run");
        out.println("res_list <runId> [--param PARAM] - show results for run");
        out.println("exit - stop the program");
    }

    private void handleExit() {
        running = false;
        out.println("CLI stopped.");
    }

    private void handleSave(ParsedCommand parsedCommand) throws IOException {
        String path = parseRequiredPathArgument(parsedCommand, "save");
        dataManager.saveToFile(path);
        out.println("Data saved to " + path);
    }

    private void handleLoad(ParsedCommand parsedCommand) throws IOException {
        String path = parseRequiredPathArgument(parsedCommand, "load");
        dataManager.loadFromFile(path);
        out.println("Data loaded from " + path);
    }

    private void handleExperimentAdd(ParsedCommand parsedCommand) {
        ensureNoArguments(parsedCommand, "exp_add");

        out.println("Creating a new experiment.");
        String name = readRequiredValue("Name");
        String description = readOptionalValue("Description");
        String ownerUsername = readRequiredValue("Owner username");

        Experiment experiment = experimentService.add(name, description, ownerUsername);
        out.println("Experiment created with id " + experiment.getId());
    }

    private void handleExperimentList(ParsedCommand parsedCommand) {
        ensureNoArguments(parsedCommand, "exp_list");

        var experiments = experimentService.list();
        if (experiments.isEmpty()) {
            out.println("No experiments found.");
            return;
        }

        out.println("Experiments:");
        for (Experiment experiment : experiments) {
            out.println(formatExperimentLine(experiment));
        }
    }

    private void handleExperimentShow(ParsedCommand parsedCommand) {
        long experimentId = parseRequiredLongArgument(parsedCommand, "exp_show", "experiment id");
        Experiment experiment = experimentService.getById(experimentId);

        out.println("Experiment details:");
        out.println("Id: " + experiment.getId());
        out.println("Name: " + experiment.getName());
        out.println("Description: " + formatNullableValue(experiment.getDescription()));
        out.println("Owner username: " + experiment.getOwnerUsername());
        out.println("Created at: " + experiment.getCreatedAt());
        out.println("Updated at: " + experiment.getUpdatedAt());
    }

    private void handleExperimentUpdate(ParsedCommand parsedCommand) {
        ExperimentUpdateRequest request = parseExperimentUpdateRequest(parsedCommand);
        Experiment experiment = experimentService.getById(request.id());

        String updatedName = experiment.getName();
        String updatedDescription = experiment.getDescription();
        String updatedOwnerUsername = experiment.getOwnerUsername();

        switch (request.field()) {
            case "name" -> updatedName = request.value();
            case "description" -> updatedDescription = request.value();
            case "ownerUsername" -> updatedOwnerUsername = request.value();
            default -> throw new ValidationException("Unknown experiment field: " + request.field());
        }

        experimentService.update(experiment.getId(), updatedName, updatedDescription, updatedOwnerUsername);
        out.println("Experiment updated.");
    }

    private void handleExperimentSummary(ParsedCommand parsedCommand) {
        long experimentId = parseRequiredLongArgument(parsedCommand, "exp_summary", "experiment id");
        ExperimentSummary summary = experimentSummaryService.summarize(experimentId);

        if (summary.isEmpty()) {
            out.println("No summary data for experiment " + summary.experimentId() + ".");
            return;
        }

        out.println("Summary for experiment " + summary.experimentId() + " (" + summary.experimentName() + "):");
        for (MeasurementParam param : MeasurementParam.values()) {
            SummaryStats stats = summary.statsByParam().get(param);
            if (stats != null) {
                out.println(formatSummaryLine(param, stats));
            }
        }
    }

    private void handleRunAdd(ParsedCommand parsedCommand) {
        long experimentId = parseRequiredLongArgument(parsedCommand, "run_add", "experiment id");

        out.println("Creating a new run.");
        String runName = readRequiredValue("Run name");
        String operatorName = readRequiredValue("Operator");

        Run run = runService.add(experimentId, runName, operatorName);
        out.println("Run created with id " + run.getId());
    }

    private void handleRunList(ParsedCommand parsedCommand) {
        long experimentId = parseRequiredLongArgument(parsedCommand, "run_list", "experiment id");
        Experiment experiment = experimentService.getById(experimentId);
        var runs = runService.listByExperimentId(experimentId);

        if (runs.isEmpty()) {
            out.println("No runs found for experiment " + experiment.getId() + ".");
            return;
        }

        out.println("Runs for experiment " + experiment.getId() + " (" + experiment.getName() + "):");
        for (Run run : runs) {
            out.println(formatRunLine(run));
        }
    }

    private void handleRunShow(ParsedCommand parsedCommand) {
        long runId = parseRequiredLongArgument(parsedCommand, "run_show", "run id");
        Run run = runService.getById(runId);
        int resultCount = runResultService.listByRunId(runId).size();

        out.println("Run details:");
        out.println("Id: " + run.getId());
        out.println("Experiment id: " + run.getExperimentId());
        out.println("Name: " + run.getName());
        out.println("Operator: " + run.getOperatorName());
        out.println("Results: " + resultCount);
        out.println("Created at: " + run.getCreatedAt());
        out.println("Updated at: " + run.getUpdatedAt());
    }

    private void handleResultAdd(ParsedCommand parsedCommand) {
        long runId = parseRequiredLongArgument(parsedCommand, "res_add", "run id");

        out.println("Creating a new result.");
        MeasurementParam param = readMeasurementParam("Parameter");
        double value = readRequiredDouble("Value");
        String unit = readRequiredValue("Unit");
        String comment = readOptionalValue("Comment");

        RunResult result = runResultService.add(runId, param, value, unit, comment);
        out.println("Result created with id " + result.getId());
    }

    private void handleResultList(ParsedCommand parsedCommand) {
        ResultListRequest request = parseResultListRequest(parsedCommand);
        Run run = runService.getById(request.runId());
        var results = runResultService.listByRunId(request.runId());

        if (request.param() != null) {
            results = results.stream()
                    .filter(result -> result.getParam() == request.param())
                    .toList();
        }

        if (results.isEmpty()) {
            out.println("No results found for run " + run.getId() + ".");
            return;
        }

        out.println("Results for run " + run.getId() + " (" + run.getName() + "):");
        for (RunResult result : results) {
            out.println(formatResultLine(result));
        }
    }

    private void ensureNoArguments(ParsedCommand parsedCommand, String commandName) {
        if (!parsedCommand.arguments().isEmpty()) {
            throw new ValidationException(commandName + " does not accept arguments");
        }
    }

    private long parseRequiredLongArgument(ParsedCommand parsedCommand, String commandName, String argumentLabel) {
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException(commandName + " requires " + argumentLabel);
        }

        String[] parts = arguments.split("\\s+");
        if (parts.length != 1) {
            throw new ValidationException(commandName + " accepts exactly one argument: " + argumentLabel);
        }

        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException(argumentLabel + " must be a number");
        }
    }

    private String parseRequiredPathArgument(ParsedCommand parsedCommand, String commandName) {
        String path = parsedCommand.arguments();
        if (path.isEmpty()) {
            throw new ValidationException(commandName + " requires file path");
        }
        return path;
    }

    private ExperimentUpdateRequest parseExperimentUpdateRequest(ParsedCommand parsedCommand) {
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException("exp_update requires experiment id and one field=value");
        }

        String[] parts = arguments.split("\\s+");
        long experimentId;
        try {
            experimentId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException("experiment id must be a number");
        }

        if (parts.length != 2) {
            throw new ValidationException("exp_update accepts exactly one field=value");
        }

        String[] fieldAndValue = parts[1].split("=", 2);
        if (fieldAndValue.length != 2) {
            throw new ValidationException("Invalid update argument: " + parts[1]);
        }

        return new ExperimentUpdateRequest(experimentId, fieldAndValue[0], fieldAndValue[1]);
    }

    private ResultListRequest parseResultListRequest(ParsedCommand parsedCommand) {
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException("res_list requires run id");
        }

        String[] parts = arguments.split("\\s+");
        if (parts.length != 1 && parts.length != 3) {
            throw new ValidationException("res_list accepts: <runId> or <runId> --param PARAM");
        }

        long runId;
        try {
            runId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException("run id must be a number");
        }

        if (parts.length == 1) {
            return new ResultListRequest(runId, null);
        }

        if (!"--param".equals(parts[1])) {
            throw new ValidationException("res_list accepts only the --param option");
        }

        MeasurementParam param = parseMeasurementParam(parts[2], "param");
        return new ResultListRequest(runId, param);
    }

    private String readRequiredValue(String label) {
        while (true) {
            out.print(label + ": ");
            out.flush();

            if (!scanner.hasNextLine()) {
                running = false;
                throw new ValidationException("Input stream was closed");
            }

            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }

            out.println(label + " can't be empty.");
        }
    }

    private double readRequiredDouble(String label) {
        while (true) {
            String rawValue = readRequiredValue(label);
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                out.println(label + " must be a number.");
            }
        }
    }

    private MeasurementParam readMeasurementParam(String label) {
        while (true) {
            String rawValue = readRequiredValue(label);

            try {
                return parseMeasurementParam(rawValue, label);
            } catch (ValidationException e) {
                out.println(e.getMessage());
            }
        }
    }

    private MeasurementParam parseMeasurementParam(String rawValue, String label) {
        for (MeasurementParam param : MeasurementParam.values()) {
            if (param.name().equalsIgnoreCase(rawValue)) {
                return param;
            }
        }

        throw new ValidationException(label + " must be one of: pH, Temperature, Concentration.");
    }

    private String readOptionalValue(String label) {
        out.print(label + ": ");
        out.flush();

        if (!scanner.hasNextLine()) {
            running = false;
            throw new ValidationException("Input stream was closed");
        }

        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private String formatExperimentLine(Experiment experiment) {
        String description = formatNullableValue(experiment.getDescription());
        return experiment.getId()
                + " | "
                + experiment.getName()
                + " | owner=" + experiment.getOwnerUsername()
                + " | description=" + description;
    }

    private String formatNullableValue(String value) {
        return value == null ? "-" : value;
    }

    private String formatRunLine(Run run) {
        return run.getId()
                + " | "
                + run.getName()
                + " | operator=" + run.getOperatorName();
    }

    private String formatResultLine(RunResult result) {
        return result.getId()
                + " | "
                + result.getParam()
                + " | value=" + result.getValue()
                + " | unit=" + result.getUnit()
                + " | comment=" + formatNullableValue(result.getComment());
    }

    private String formatSummaryLine(MeasurementParam param, SummaryStats stats) {
        return param
                + ": count=" + stats.count()
                + " min=" + formatDecimal(stats.min())
                + " max=" + formatDecimal(stats.max())
                + " avg=" + formatDecimal(stats.avg());
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record ExperimentUpdateRequest(long id, String field, String value) {
    }

    private record ResultListRequest(long runId, MeasurementParam param) {
    }

    private record ParsedCommand(String name, String arguments) {
    }
}
