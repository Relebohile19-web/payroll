package com.example.payroll_management;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.payroll_management.DatabaseConnection.getConnection;

public class loginController {

    @FXML private AnchorPane Anchorpane;
    @FXML private Button login;
    @FXML private Button signup;
    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private ChoiceBox<String> choicebox;
    @FXML private Label welcome;
    @FXML private Label account;

    public static String loggedInEmployeeId; // Store employee ID for Employee role

    @FXML
    public void initialize() {
        // Populate ChoiceBox
        choicebox.getItems().addAll("Admin", "Employee");

        // Apply fade-in animation to the entire form
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), Anchorpane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        // Login button action
        login.setOnAction(event -> {
            String role = choicebox.getValue();
            String user = username.getText().trim(); // Trim to remove accidental spaces
            String pass = password.getText().trim();

            // Validate inputs
            if (user.isEmpty() || pass.isEmpty()) {
                showAlert("Please enter both username and password.");
                password.clear(); // Clear password for security
                return;
            }

            if (role == null) {
                showAlert("Please select a role (Admin or Employee).");
                password.clear();
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "SELECT role, employee_id FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, user);
                    stmt.setString(2, pass);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbRole = rs.getString("role");
                            loggedInEmployeeId = rs.getString("employee_id");

                            // Compare roles ignoring case
                            if (!dbRole.equalsIgnoreCase(role)) {
                                showAlert("Selected role does not match your account's role.");
                                password.clear();
                                return;
                            }

                            // Navigate based on role
                            if (role.equalsIgnoreCase("Admin")) {
                                HelloApplication.setRoot("admin_dashboard.fxml");
                            } else if (role.equalsIgnoreCase("Employee")) {
                                if (loggedInEmployeeId == null || loggedInEmployeeId.isEmpty()) {
                                    showAlert("Employee ID not found. Please contact an administrator.");
                                    password.clear();
                                    return;
                                }
                                HelloApplication.setRoot("employee_dashboard.fxml");
                            }
                        } else {
                            showAlert("Invalid username or password.");
                            password.clear();
                        }
                    }
                }
            } catch (SQLException e) {
                // Log the error (in production, use a logging framework like SLF4J)
                System.err.println("Database error during login: " + e.getMessage());
                showAlert("Login failed due to a database error. Please try again later.");
                password.clear();
            } catch (Exception e) {
                // Log the error
                System.err.println("Unexpected error during login: " + e.getMessage());
                showAlert("An unexpected error occurred. Please try again.");
                password.clear();
            }
        });

        // Signup button action
        signup.setOnAction(event -> {
            try {
                HelloApplication.setRoot("signup.fxml");
            } catch (Exception e) {
                System.err.println("Error navigating to signup: " + e.getMessage());
                showAlert("Failed to load signup page. Please try again.");
            }
        });
    }

    // Hover effect for Signup button (scale animation + color change)
    @FXML
    private void handleSignupMouseEntered(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), signup);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.play();

        signup.setStyle("-fx-background-color: #ec407a; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }

    @FXML
    private void handleSignupMouseExited(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), signup);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        signup.setStyle("-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }

    // Hover effect for Login button (scale animation + color change)
    @FXML
    private void handleLoginMouseEntered(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), login);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.play();

        login.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 5, 0.3, 0, 0);");
    }

    @FXML
    private void handleLoginMouseExited(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), login);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        login.setStyle("-fx-background-color: #ab47bc; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 5, 0.3, 0, 0);");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}