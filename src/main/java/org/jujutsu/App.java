package org.jujutsu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jujutsu.ui.MainWindow;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainWindow window = new MainWindow(primaryStage);

        Scene scene = new Scene(window.buildLayout(), 1000, 700);
        primaryStage.setTitle("Анализатор миссий — Токийский магический колледж");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
