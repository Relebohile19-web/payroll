package com.example.payroll_management;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignupController {

    @FXML private AnchorPane anchorPane;
    @FXML private TextField fullName, Email, UserName, Password, confirm;
    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button signup, login;
    @FXML private Label bestie, here;

    @FXML
    public void initialize() {
        bestie.setText("Welcome Bestie!!!");
        choiceBox.getItems().addAll("Admin", "Employee");
    }

    @FXML
    private void handleSignup() {
        String fullNameText = fullName.getText().trim();
        String emailText = Email.getText().trim();
        String username = UserName.getText().trim();
        String password = Password.getText().trim();
        String confirmPassword = confirm.getText().trim();
        String role = choiceBox.getValue();

        // Validation
        if (fullNameText.isEmpty() || emailText.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showErrorAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorAlert("Validation Error", "Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showErrorAlert("Error", "Username already exists.");
                return;
            }

            // Insert new user (user_id will auto-increment)
            PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password); // In production, hash the password
            userStmt.setString(3, role);
            userStmt.executeUpdate();

            // Get the auto-generated user_id
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                // If the role is Employee, insert into employees table
                if (role.equals("Employee")) {
                    PreparedStatement empStmt = conn.prepareStatement(
                            "INSERT INTO employees (employee_id, name, department, user_id) VALUES (?, ?, ?, ?)");
                    empStmt.setString(1, generateEmployeeId());
                    empStmt.setString(2, fullNameText);
                    empStmt.setString(3, "Unassigned");
                    empStmt.setInt(4, userId);
                    empStmt.executeUpdate();
                }

                showInfoAlert("Success", "Registration successful! Please log in.");
                clearFields();
                navigateTo("hello-view");
            } else {
                showErrorAlert("Error", "Failed to retrieve generated user_id.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        try {
            HelloApplication.setRoot("hello-view");
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load login page: " + e.getMessage());
        }
    }

    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }

    private void clearFields() {
        fullName.clear();
        Email.clear();
        UserName.clear();
        Password.clear();
        confirm.clear();
        choiceBox.getSelectionModel().clearSelection();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlFile) {
        try {
            HelloApplication.setRoot(fxmlFile);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load " + fxmlFile + ".fxml. Please ensure the file exists.");
        }
    }

    @FXML
    private void handleSignupMouseEntered(MouseEvent event) {
        signup.setStyle(
                "-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 10, 0.5, 0, 0);"
        );
    }

    @FXML
    private void handleSignupMouseExited(MouseEvent event) {
        signup.setStyle(
                "-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);"
        );
    }

    @FXML
    private void handleLoginMouseEntered(MouseEvent event) {
        login.setStyle(
                "-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 10, 0.5, 0, 0);"
        );
    }

    @FXML
    private void handleLoginMouseExited(MouseEvent event) {
        login.setStyle(
                "-fx-background-color: #ab47bc; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; " +
                        "-fx-background-radius: 25; -fx-effect: dropshadow(gaussian, #8e24aa, 5, 0.3, 0, 0);"
        );
    }
}