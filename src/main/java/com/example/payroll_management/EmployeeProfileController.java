package com.example.payroll_management;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class EmployeeProfileController {

    @FXML private AnchorPane anchorpane;
    @FXML private AnchorPane sidebar;
    @FXML private Label payroll, profileTitle;
    @FXML private Label dashboard, myprofile, payslip, userLabel;
    @FXML private Label basicSalary, workingHours, netSalary;
    @FXML private Label employeeId, department, position, joinDate;
    @FXML private Button logout;

    @FXML
    public void initialize() {
        // Apply fade-in animation to the entire page
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), anchorpane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        // Set up navigation for labels
        setupNavigation(dashboard, "employee_dashboard.fxml");
        setupNavigation(myprofile, "employee_profile.fxml"); // Current page
        setupNavigation(payslip, "employee_payslip.fxml");

        // Set up logout button
        if (logout != null) {
            logout.setOnAction(event -> {
                loginController.loggedInEmployeeId = null; // Clear session data
                navigateTo("hello-view.fxml");
            });
        }
    }

    private void setupNavigation(Label label, String fxmlFile) {
        if (label != null) {
            label.setOnMouseClicked((MouseEvent event) -> navigateTo(fxmlFile));
            label.setStyle(label.getStyle() + "; -fx-cursor: hand;");
        }
    }

    private void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/payroll_management/" + fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) (logout != null ? logout.getScene().getWindow() :
                    dashboard != null ? dashboard.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load: " + fxmlFile + "\n" + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogoutMouseEntered(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), logout);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.play();
        logout.setStyle("-fx-background-color: #ec407a; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }

    @FXML
    private void handleLogoutMouseExited(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), logout);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
        logout.setStyle("-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }
}