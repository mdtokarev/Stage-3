package ui;

import javafx.application.Application;

public final class UiLauncher {
    private UiLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(UiMain.class, args);
    }
}
