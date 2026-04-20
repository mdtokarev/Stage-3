package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.DataManager;
import service.ExperimentService;
import service.ExperimentSummaryService;
import service.RunResultService;
import service.RunService;
import ui.controller.MainController;

public class UiMain extends Application {

    @Override
    public void start(Stage stage) {
        ExperimentService experimentService = new ExperimentService();
        RunService runService = new RunService(experimentService);
        RunResultService runResultService = new RunResultService(runService);
        DataManager dataManager = new DataManager(experimentService, runService, runResultService);
        ExperimentSummaryService experimentSummaryService =
                new ExperimentSummaryService(experimentService, runService, runResultService);

        MainController controller = new MainController(
                stage,
                experimentService,
                runService,
                runResultService,
                dataManager,
                experimentSummaryService
        );

        Scene scene = new Scene(controller.createContent(), 1540, 860);
        stage.setTitle("Laba1 UI - Experiments");
        stage.setMinWidth(1200);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }
}
