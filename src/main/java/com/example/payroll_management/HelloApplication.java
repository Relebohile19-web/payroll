package com.example.payroll_management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static Stage primaryStage;

    public static void setRootWithEmployeeId(String employeeDashboard, String employeeId) {

    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setRoot("hello-view.fxml");
        stage.setTitle("Payroll Management System");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = FXMLLoader.load(HelloApplication.class.getResource(fxml));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}