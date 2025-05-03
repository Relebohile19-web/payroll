package com.example.payroll_management;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Employee_dashboardController {

    @FXML private AnchorPane anchorPane;
    @FXML private Button logout;
    @FXML private Label welcomeLabel, basicSalary, salaryDetails, payslips;
    @FXML private TableView<Payslip> payslipTable;
    @FXML private TableColumn<Payslip, String> monthColumn, yearColumn, basicSalaryColumn;

    private ObservableList<Payslip> payslipData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Apply fade-in animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), anchorPane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        // Set up payslip table columns
        monthColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMonth()));
        yearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getYear()));
        basicSalaryColumn.setCellValueFactory(cellData -> new SimpleStringProperty("R" + String.format("%,.0f", cellData.getValue().getBasicSalary())));

        // Set up navigation
        salaryDetails.setOnMouseClicked(event -> showSalaryDetails());
        payslips.setOnMouseClicked(event -> showPayslips());
        logout.setOnAction(event -> handleLogout());

        // Set default view
        showSalaryDetails();
        setActiveLabel(salaryDetails);
    }

    private void showSalaryDetails() {
        payslipTable.setVisible(false);
        welcomeLabel.setVisible(true);
        basicSalary.setVisible(true);

        // Load employee's salary details
        String userId = UserSession.getInstance().getUserId();
        welcomeLabel.setText("Welcome, " + UserSession.getInstance().getUsername());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT basic_salary FROM employees WHERE user_id = ?")) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double salary = rs.getDouble("basic_salary");
                basicSalary.setText(String.format("R%,.0f", salary));
            } else {
                basicSalary.setText("N/A");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load salary details: " + e.getMessage());
        }
    }

    private void showPayslips() {
        welcomeLabel.setVisible(false);
        basicSalary.setVisible(false);
        payslipTable.setVisible(true);

        // Load employee's payslips
        String userId = UserSession.getInstance().getUserId();
        payslipData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT month, year, basic_salary FROM payslips WHERE user_id = ?")) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payslipData.add(new Payslip(rs.getString("month"), rs.getString("year"), rs.getDouble("basic_salary")));
            }
            payslipTable.setItems(payslipData);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load payslips: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().clear();
            HelloApplication.setRoot("hello-view.fxml");
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void setActiveLabel(Label label) {
        // Reset styles
        salaryDetails.setStyle("-fx-text-fill: #333; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-padding: 0 0 0 30;");
        payslips.setStyle("-fx-text-fill: #333; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-padding: 0 0 0 30;");

        // Set active style
        if (label == salaryDetails) {
            salaryDetails.setStyle("-fx-text-fill: #333; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-color: #f5d6e6; -fx-background-radius: 5; -fx-padding: 0 0 0 30;");
        } else if (label == payslips) {
            payslips.setStyle("-fx-text-fill: #333; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-color: #f5d6e6; -fx-background-radius: 5; -fx-padding: 0 0 0 30;");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setEmployeeId(String employeeId) {

    }

    public static class Payslip {
        private final String month;
        private final String year;
        private final double basicSalary;

        public Payslip(String month, String year, double basicSalary) {
            this.month = month;
            this.year = year;
            this.basicSalary = basicSalary;
        }

        public String getMonth() {
            return month;
        }

        public String getYear() {
            return year;
        }

        public double getBasicSalary() {
            return basicSalary;
        }
    }
}