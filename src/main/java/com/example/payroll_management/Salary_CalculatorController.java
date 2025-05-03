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

public class Salary_CalculatorController {

    // Navigation elements
    @FXML private AnchorPane anchorPane;
    @FXML private AnchorPane sidebar;
    @FXML private AnchorPane contentPane;
    @FXML private Label dashboard, employees, calculator, payslip, reports, payroll, adminUser, salaryCalculatorTitle;
    @FXML private Button logout;

    // Calculator elements
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField basicSalaryField, workingHoursField, overtimeHoursField, otherDeductionsField;
    @FXML private Slider taxRateSlider, insuranceRateSlider;
    @FXML private Label overtimeRateLabel, taxRateLabel, insuranceRateLabel;
    @FXML private Label resultsLabel, netSalaryBreakdown, deductionsBreakdown;
    @FXML private Button resetButton;

    private double basicSalary = 5000;
    private double workingHours = 160;
    private double overtimeHours = 10;
    private final double overtimeRate = 1.5; // Fixed, as no slider in FXML
    private double taxRate = 0.2;
    private double insuranceRate = 0.05;
    private double otherDeductions = 100;

    @FXML
    public void initialize() {
        // Apply fade-in animation to the entire page
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), anchorPane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        // Initialize navigation
        setupNavigation();

        // Initialize calculator components
        populateEmployeeComboBox();
        employeeComboBox.getSelectionModel().selectFirst();

        basicSalaryField.setText(String.valueOf(basicSalary));
        workingHoursField.setText(String.valueOf(workingHours));
        overtimeHoursField.setText(String.valueOf(overtimeHours));
        otherDeductionsField.setText(String.valueOf(otherDeductions));

        taxRateSlider.setValue(taxRate * 100);
        insuranceRateSlider.setValue(insuranceRate * 100);

        updateRateLabels();
        setupListeners();
        calculateSalary();
    }

    private void setupNavigation() {
        configureNavigationLabel(dashboard, "admin_dashboard");
        configureNavigationLabel(employees, "Employee");
        // Skip calculator since we're already on Salary_Calculator.fxml
        configureNavigationLabel(payslip, "payslip");
        configureNavigationLabel(reports, "reports");

        // Set logout button action
        logout.setOnAction(event -> {
            loginController.loggedInEmployeeId = null;
            navigateTo("hello-view");
        });
    }

    private void configureNavigationLabel(Label label, String fxmlFile) {
        if (label == null) {
            System.err.println("Navigation label is null for " + fxmlFile + ". Check FXML configuration.");
            return;
        }
        label.setStyle(label.getStyle() + "; -fx-cursor: hand;");
        label.setOnMouseClicked(event -> navigateTo(fxmlFile));
    }

    private void populateEmployeeComboBox() {
        employeeComboBox.getItems().add("Custom");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT employee_id, name, basic_salary, working_hours FROM employees";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String employeeName = rs.getString("name");
                employeeComboBox.getItems().add(employeeName);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employees: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        employeeComboBox.setOnAction(event -> {
            String selected = employeeComboBox.getValue();
            if ("Custom".equals(selected)) {
                basicSalaryField.setText("5000");
                workingHoursField.setText("160");
            } else {
                loadEmployeeData(selected);
            }
            calculateSalary();
        });
    }

    private void loadEmployeeData(String employeeName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT basic_salary, working_hours FROM employees WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, employeeName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                basicSalary = rs.getDouble("basic_salary");
                workingHours = rs.getDouble("working_hours");
                basicSalaryField.setText(String.valueOf(basicSalary));
                workingHoursField.setText(String.valueOf(workingHours));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employee data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupListeners() {
        taxRateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            taxRate = newVal.doubleValue() / 100;
            updateRateLabels();
            calculateSalary();
        });

        insuranceRateSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            insuranceRate = newVal.doubleValue() / 100;
            updateRateLabels();
            calculateSalary();
        });

        basicSalaryField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                basicSalary = Double.parseDouble(newVal);
                calculateSalary();
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Basic Salary must be a number", Alert.AlertType.WARNING);
            }
        });

        workingHoursField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                workingHours = Double.parseDouble(newVal);
                calculateSalary();
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Working Hours must be a number", Alert.AlertType.WARNING);
            }
        });

        overtimeHoursField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                overtimeHours = Double.parseDouble(newVal);
                calculateSalary();
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Overtime Hours must be a number", Alert.AlertType.WARNING);
            }
        });

        otherDeductionsField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                otherDeductions = Double.parseDouble(newVal);
                calculateSalary();
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Other Deductions must be a number", Alert.AlertType.WARNING);
            }
        });
    }

    private void navigateTo(String fxmlFile) {
        try {
            // Debug: Print the resource path being loaded
            String resourcePath = fxmlFile + ".fxml";
            System.out.println("Attempting to load FXML: " + resourcePath);
            if (HelloApplication.class.getResource(resourcePath) == null) {
                throw new Exception("Resource not found: " + resourcePath);
            }
            HelloApplication.setRoot(fxmlFile);
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load " + fxmlFile + ".fxml. Please ensure the file exists.", Alert.AlertType.ERROR);
            System.err.println("Navigation failed for " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateRateLabels() {
        overtimeRateLabel.setText("Overtime Rate: 1.5x");
        taxRateLabel.setText(String.format("Tax Rate: %.1f%%", taxRate * 100));
        insuranceRateLabel.setText(String.format("Insurance Rate: %.1f%%", insuranceRate * 100));
    }

    private void calculateSalary() {
        try {
            double hourlyRate = workingHours != 0 ? basicSalary / workingHours : 0;
            double overtimePay = overtimeHours * hourlyRate * overtimeRate;
            double grossSalary = basicSalary + overtimePay;

            double taxDeduction = grossSalary * taxRate;
            double insuranceDeduction = grossSalary * insuranceRate;
            double totalDeductions = taxDeduction + insuranceDeduction + otherDeductions;

            double netSalary = grossSalary - totalDeductions;
            double netSalaryPercentage = grossSalary != 0 ? (netSalary / grossSalary) * 100 : 0;
            double deductionsPercentage = grossSalary != 0 ? (totalDeductions / grossSalary) * 100 : 0;

            resultsLabel.setText(String.format("""
                EARNINGS
                Basic Salary: R%.2f
                Hourly Rate: R%.2f/hr
                Overtime Pay: R%.2f
                Gross Salary: R%.2f

                DEDUCTIONS
                Tax (%.1f%%): -R%.2f
                Insurance (%.1f%%): -R%.2f
                Other Deductions: -R%.2f
                Total Deductions: -R%.2f

                Net Salary: R%.2f
                """,
                    basicSalary,
                    hourlyRate,
                    overtimePay,
                    grossSalary,
                    taxRate * 100, taxDeduction,
                    insuranceRate * 100, insuranceDeduction,
                    otherDeductions,
                    totalDeductions,
                    netSalary
            ));

            netSalaryBreakdown.setText(String.format("Net Salary: %.1f%%", netSalaryPercentage));
            deductionsBreakdown.setText(String.format("Deductions: %.1f%%", deductionsPercentage));
        } catch (Exception e) {
            resultsLabel.setText("Error calculating salary: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        basicSalaryField.setText("5000");
        workingHoursField.setText("160");
        overtimeHoursField.setText("10");
        otherDeductionsField.setText("100");

        taxRateSlider.setValue(20);
        insuranceRateSlider.setValue(5);

        employeeComboBox.getSelectionModel().select("Custom");
    }

    // Hover effect for Logout button
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

    // Hover effect for Reset button
    @FXML
    private void handleResetMouseEntered(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), resetButton);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.play();

        resetButton.setStyle("-fx-background-color: #ec407a; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }

    @FXML
    private void handleResetMouseExited(MouseEvent event) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), resetButton);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();

        resetButton.setStyle("-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #d81b60, 5, 0.3, 0, 0);");
    }
}