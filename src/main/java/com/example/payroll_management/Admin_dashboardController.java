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
import java.time.LocalDate;

public class Admin_dashboardController {

    @FXML private AnchorPane anchorPane;
    @FXML private AnchorPane sidebar;
    @FXML private Button logout;
    @FXML private Label appName, dashboard, manageEmployees, payrollReports;

    // Manage Employees Section
    @FXML private TextField empIdField, empNameField, empDeptField, empPositionField, empSalaryField, empHoursField;
    @FXML private DatePicker empJoinDatePicker;
    @FXML private Button addEmployeeButton;

    // Payroll Reports Section
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField payslipMonthField, payslipYearField;
    @FXML private Button generatePayslipButton;
    @FXML private TableView<Payslip> payslipTable;
    @FXML private TableColumn<Payslip, String> monthColumn, yearColumn, nameColumn, departmentColumn, positionColumn, joinDateColumn, basicSalaryColumn, workingHoursColumn;

    private Label activeLabel;
    private ObservableList<Payslip> payslipData = FXCollections.observableArrayList();
    private ObservableList<String> employeeIds = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), anchorPane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        // Navigation setup
        setupNavigation(dashboard, this::showDashboard);
        setupNavigation(manageEmployees, this::showManageEmployees);
        setupNavigation(payrollReports, this::showPayrollReports);

        // Set up table columns
        monthColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMonth()));
        yearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getYear()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        departmentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        joinDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJoinDate()));
        basicSalaryColumn.setCellValueFactory(cellData -> new SimpleStringProperty("R" + String.format("%,.0f", cellData.getValue().getBasicSalary())));
        workingHoursColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getWorkingHours())));

        // Populate employee ComboBox
        populateEmployeeComboBox();

        // Set up button actions
        addEmployeeButton.setOnAction(event -> addEmployee());
        generatePayslipButton.setOnAction(event -> generatePayslip());
        logout.setOnAction(event -> navigateTo("login.fxml"));

        setActiveLabel(dashboard);
        showDashboard();
    }

    private void showDashboard() {
        hideAllSections();
        // Add dashboard content if needed
    }

    private void showManageEmployees() {
        hideAllSections();
        empIdField.setVisible(true);
        empNameField.setVisible(true);
        empDeptField.setVisible(true);
        empPositionField.setVisible(true);
        empJoinDatePicker.setVisible(true);
        empSalaryField.setVisible(true);
        empHoursField.setVisible(true);
        addEmployeeButton.setVisible(true);
    }

    private void showPayrollReports() {
        hideAllSections();
        employeeComboBox.setVisible(true);
        payslipMonthField.setVisible(true);
        payslipYearField.setVisible(true);
        generatePayslipButton.setVisible(true);
        payslipTable.setVisible(true);
        refreshPayslipTable();
    }

    private void hideAllSections() {
        empIdField.setVisible(false);
        empNameField.setVisible(false);
        empDeptField.setVisible(false);
        empPositionField.setVisible(false);
        empJoinDatePicker.setVisible(false);
        empSalaryField.setVisible(false);
        empHoursField.setVisible(false);
        addEmployeeButton.setVisible(false);
        employeeComboBox.setVisible(false);
        payslipMonthField.setVisible(false);
        payslipYearField.setVisible(false);
        generatePayslipButton.setVisible(false);
        payslipTable.setVisible(false);
    }

    private void populateEmployeeComboBox() {
        employeeIds.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT employee_id FROM employees");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                employeeIds.add(rs.getString("employee_id"));
            }
            employeeComboBox.setItems(employeeIds);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load employees: " + e.getMessage());
        }
    }

    private void addEmployee() {
        String empId = empIdField.getText().trim();
        String name = empNameField.getText().trim();
        String dept = empDeptField.getText().trim();
        String position = empPositionField.getText().trim();
        LocalDate joinDate = empJoinDatePicker.getValue();
        String salaryText = empSalaryField.getText().trim();
        String hoursText = empHoursField.getText().trim();

        // Validation
        if (empId.isEmpty() || name.isEmpty() || salaryText.isEmpty() || hoursText.isEmpty()) {
            showErrorAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        double salary;
        double hours;
        try {
            salary = Double.parseDouble(salaryText);
            hours = Double.parseDouble(hoursText);
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Salary and working hours must be numeric values.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO employees (employee_id, name, department, position, join_date, basic_salary, working_hours, user_id) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, NULL)")) {
            stmt.setString(1, empId);
            stmt.setString(2, name);
            stmt.setString(3, dept.isEmpty() ? null : dept);
            stmt.setString(4, position.isEmpty() ? null : position);
            stmt.setDate(5, joinDate != null ? java.sql.Date.valueOf(joinDate) : null);
            stmt.setDouble(6, salary);
            stmt.setDouble(7, hours);
            stmt.executeUpdate();

            showInfoAlert("Success", "Employee added successfully.");
            populateEmployeeComboBox(); // Refresh employee list
            clearEmployeeForm();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add employee: " + e.getMessage());
        }
    }

    private void generatePayslip() {
        String selectedEmployeeId = employeeComboBox.getValue();
        String month = payslipMonthField.getText().trim();
        String year = payslipYearField.getText().trim();

        // Validation
        if (selectedEmployeeId == null || month.isEmpty() || year.isEmpty()) {
            showErrorAlert("Validation Error", "Please select an employee and enter month and year.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement empStmt = conn.prepareStatement(
                     "SELECT name, department, position, join_date, basic_salary, working_hours " +
                             "FROM employees WHERE employee_id = ?")) {
            empStmt.setString(1, selectedEmployeeId);
            ResultSet rs = empStmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String dept = rs.getString("department");
                String position = rs.getString("position");
                java.sql.Date joinDate = rs.getDate("join_date");
                double salary = rs.getDouble("basic_salary");
                double hours = rs.getDouble("working_hours");

                // Insert payslip
                try (PreparedStatement payslipStmt = conn.prepareStatement(
                        "INSERT INTO payslips (employee_id, user_id, name, department, position, join_date, basic_salary, working_hours, month, year) " +
                                "VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    payslipStmt.setString(1, selectedEmployeeId);
                    payslipStmt.setString(2, name);
                    payslipStmt.setString(3, dept);
                    payslipStmt.setString(4, position);
                    payslipStmt.setDate(5, joinDate);
                    payslipStmt.setDouble(6, salary);
                    payslipStmt.setDouble(7, hours);
                    payslipStmt.setString(8, month);
                    payslipStmt.setString(9, year);
                    payslipStmt.executeUpdate();

                    showInfoAlert("Success", "Payslip generated successfully.");
                    refreshPayslipTable();
                    clearPayslipForm();
                }
            } else {
                showErrorAlert("Error", "Selected employee not found.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to generate payslip: " + e.getMessage());
        }
    }

    private void refreshPayslipTable() {
        payslipData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name, department, position, join_date, basic_salary, working_hours, month, year " +
                             "FROM payslips")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payslipData.add(new Payslip(
                        rs.getString("month"),
                        rs.getString("year"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getString("join_date"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours")
                ));
            }
            payslipTable.setItems(payslipData);
            if (payslipData.isEmpty()) {
                payslipTable.setPlaceholder(new Label("No payslips available"));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load payslips: " + e.getMessage());
        }
    }

    private void clearEmployeeForm() {
        empIdField.clear();
        empNameField.clear();
        empDeptField.clear();
        empPositionField.clear();
        empJoinDatePicker.setValue(null);
        empSalaryField.clear();
        empHoursField.clear();
    }

    private void clearPayslipForm() {
        employeeComboBox.getSelectionModel().clearSelection();
        payslipMonthField.clear();
        payslipYearField.clear();
    }

    private void setupNavigation(Label label, Runnable action) {
        if (label != null) {
            label.setStyle(label.getStyle() + "; -fx-cursor: hand;");
            label.setOnMouseClicked(event -> {
                setActiveLabel(label);
                action.run();
            });
        }
    }

    private void setActiveLabel(Label label) {
        if (label == null) return;

        String defaultBackgroundStyle = "; -fx-background-color: transparent; -fx-background-radius: 0;";
        String activeBackgroundStyle = "; -fx-background-color: #f5d6e6; -fx-background-radius: 5;";

        Label[] navLabels = {dashboard, manageEmployees, payrollReports};
        for (Label navLabel : navLabels) {
            if (navLabel != null) {
                String currentStyle = navLabel.getStyle();
                currentStyle = currentStyle.replaceAll("-fx-background-color:[^;]*;", "");
                currentStyle = currentStyle.replaceAll("-fx-background-radius:[^;]*;", "");
                navLabel.setStyle(currentStyle + defaultBackgroundStyle);
            }
        }

        String currentStyle = label.getStyle();
        currentStyle = currentStyle.replaceAll("-fx-background-color:[^;]*;", "");
        currentStyle = currentStyle.replaceAll("-fx-background-radius:[^;]*;", "");
        label.setStyle(currentStyle + activeBackgroundStyle);
        activeLabel = label;
    }

    @FXML
    private void navigateTo(String fxmlFile) {
        try {
            UserSession.getInstance().clear();
            HelloApplication.setRoot(fxmlFile);
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to load " + fxmlFile + ": " + e.getMessage());
        }
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

    public static class Payslip {
        private final String month;
        private final String year;
        private final String name;
        private final String department;
        private final String position;
        private final String joinDate;
        private final double basicSalary;
        private final double workingHours;

        public Payslip(String month, String year, String name, String department, String position, String joinDate, double basicSalary, double workingHours) {
            this.month = month;
            this.year = year;
            this.name = name;
            this.department = department;
            this.position = position;
            this.joinDate = joinDate;
            this.basicSalary = basicSalary;
            this.workingHours = workingHours;
        }

        public String getMonth() {
            return month;
        }

        public String getYear() {
            return year;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department != null ? department : "N/A";
        }

        public String getPosition() {
            return position != null ? position : "N/A";
        }

        public String getJoinDate() {
            return joinDate != null ? joinDate : "N/A";
        }

        public double getBasicSalary() {
            return basicSalary;
        }

        public double getWorkingHours() {
            return workingHours;
        }
    }
}