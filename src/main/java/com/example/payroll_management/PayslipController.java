package com.example.payroll_management;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PayslipController {

    @FXML private AnchorPane anchorpane;
    @FXML private AnchorPane sidebar;
    @FXML private Label Dashboard, employees, calculator, payslip, reports;
    @FXML private Button logout;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField payslipMonthField, payslipYearField;
    @FXML private Button generatePayslipButton;
    @FXML private TableView<PayslipController.Payslip> payslipTable;
    @FXML private TableColumn<PayslipController.Payslip, String> monthColumn;
    @FXML private TableColumn<PayslipController.Payslip, String> yearColumn;
    @FXML private TableColumn<PayslipController.Payslip, String> nameColumn;
    @FXML private TableColumn<PayslipController.Payslip, String> departmentColumn;
    @FXML private TableColumn<PayslipController.Payslip, String> positionColumn;
    @FXML private TableColumn<PayslipController.Payslip, String> joinDateColumn;
    @FXML private TableColumn<PayslipController.Payslip, Double> basicSalaryColumn;
    @FXML private TableColumn<PayslipController.Payslip, Double> workingHoursColumn;
    @FXML private TableColumn<PayslipController.Payslip, Double> netSalaryColumn;
    @FXML private TableColumn<PayslipController.Payslip, Double> overtimeHoursColumn;
    @FXML private TableColumn<PayslipController.Payslip, Void> actionColumn;

    private ObservableList<PayslipController.Payslip> payslipData = FXCollections.observableArrayList();
    private ObservableList<String> employeeIds = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), anchorpane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        setupNavigation(Dashboard, "admin_dashboard.fxml");
        setupNavigation(employees, "employee.fxml");
        setupNavigation(calculator, "Salary_Calculator.fxml");
        setupNavigation(payslip, "payslip.fxml");
        setupNavigation(reports, "reports.fxml");

        logout.setOnAction(event -> {
            loginController.loggedInEmployeeId = null;
            navigateTo("hello-view.fxml");
        });

        populateEmployeeComboBox();

        monthColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMonth()));
        yearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getYear()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        departmentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        joinDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJoinDate()));

        basicSalaryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBasicSalary()));
        basicSalaryColumn.setCellFactory(column -> new TableCell<PayslipController.Payslip, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("R" + String.format("%,.0f", item));
            }
        });

        workingHoursColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getWorkingHours()));
        workingHoursColumn.setCellFactory(column -> new TableCell<PayslipController.Payslip, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.2f", item));
            }
        });

        netSalaryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNetSalary()));
        netSalaryColumn.setCellFactory(column -> new TableCell<PayslipController.Payslip, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("R" + String.format("%,.0f", item));
            }
        });

        overtimeHoursColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOvertimeHours()));
        overtimeHoursColumn.setCellFactory(column -> new TableCell<PayslipController.Payslip, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.2f", item));
            }
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setStyle("-fx-background-color: #f06292; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 12; -fx-background-radius: 15;");
                viewButton.setOnAction(event -> {
                    PayslipController.Payslip payslip = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Payslip Details");
                    alert.setHeaderText("Payslip for " + payslip.getName());
                    alert.setContentText(
                            "Month: " + payslip.getMonth() + "\n" +
                                    "Year: " + payslip.getYear() + "\n" +
                                    "Name: " + payslip.getName() + "\n" +
                                    "Department: " + payslip.getDepartment() + "\n" +
                                    "Position: " + payslip.getPosition() + "\n" +
                                    "Join Date: " + payslip.getJoinDate() + "\n" +
                                    "Basic Salary: R" + String.format("%,.0f", payslip.getBasicSalary()) + "\n" +
                                    "Net Salary: R" + String.format("%,.0f", payslip.getNetSalary()) + "\n" +
                                    "Working Hours: " + String.format("%.2f", payslip.getWorkingHours()) + "\n" +
                                    "Overtime Hours: " + String.format("%.2f", payslip.getOvertimeHours())
                    );
                    alert.showAndWait();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(viewButton);
            }
        });

        generatePayslipButton.setOnAction(event -> generatePayslip());
        loadPayslipData();
    }

    private void populateEmployeeComboBox() {
        employeeIds.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT employee_id FROM employees");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) employeeIds.add(rs.getString("employee_id"));
            employeeComboBox.setItems(employeeIds);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load employees: " + e.getMessage());
        }
    }

    private void generatePayslip() {
        String selectedEmployeeId = employeeComboBox.getValue();
        String month = payslipMonthField.getText().trim();
        String year = payslipYearField.getText().trim();

        if (selectedEmployeeId == null || month.isEmpty() || year.isEmpty()) {
            showErrorAlert("Validation Error", "Please select an employee and enter month and year.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement empStmt = conn.prepareStatement(
                     "SELECT name, department, position, join_date, basic_salary, working_hours, overtime_hours FROM employees WHERE employee_id = ?")) {
            empStmt.setString(1, selectedEmployeeId);
            ResultSet rs = empStmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String department = rs.getString("department");
                String position = rs.getString("position");
                String joinDate = rs.getString("join_date");
                double basicSalary = rs.getDouble("basic_salary");
                double workingHours = rs.getDouble("working_hours");
                double overtimeHours = rs.getDouble("overtime_hours");
                double netSalary = basicSalary * (1 - 0.20 - 0.05);

                try (PreparedStatement payslipStmt = conn.prepareStatement(
                        "INSERT INTO payslips (employee_id, user_id, month, year, name, department, position, join_date, basic_salary, working_hours, net_salary, overtime_hours) " +
                                "VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    payslipStmt.setString(1, selectedEmployeeId);
                    payslipStmt.setString(2, month);
                    payslipStmt.setString(3, year);
                    payslipStmt.setString(4, name);
                    payslipStmt.setString(5, department);
                    payslipStmt.setString(6, position);
                    payslipStmt.setString(7, joinDate);
                    payslipStmt.setDouble(8, basicSalary);
                    payslipStmt.setDouble(9, workingHours);
                    payslipStmt.setDouble(10, netSalary);
                    payslipStmt.setDouble(11, overtimeHours);
                    payslipStmt.executeUpdate();

                    showInfoAlert("Success", "Payslip generated successfully.");
                    loadPayslipData();
                    clearPayslipForm();
                }
            } else {
                showErrorAlert("Error", "Selected employee not found.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to generate payslip: " + e.getMessage());
        }
    }

    private void loadPayslipData() {
        payslipData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.month, p.year, e.name, e.department, e.position, e.join_date, e.basic_salary, e.working_hours, p.net_salary, e.overtime_hours " +
                             "FROM payslips p JOIN employees e ON p.employee_id = e.employee_id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payslipData.add(new PayslipController.Payslip(
                        rs.getString("month"),
                        rs.getString("year"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getString("join_date"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours"),
                        rs.getDouble("net_salary"),
                        rs.getDouble("overtime_hours")
                ));
            }
            payslipTable.setItems(payslipData);
            if (payslipData.isEmpty()) payslipTable.setPlaceholder(new Label("No payslips available"));
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load payslips: " + e.getMessage());
        }
    }

    private void clearPayslipForm() {
        employeeComboBox.getSelectionModel().clearSelection();
        payslipMonthField.clear();
        payslipYearField.clear();
    }

    private void setupNavigation(Label label, String fxmlFile) {
        if (label != null) {
            label.setStyle(label.getStyle() + "; -fx-cursor: hand;");
            label.setOnMouseClicked(event -> navigateTo(fxmlFile));
        }
    }

    private void navigateTo(String fxmlFile) {
        try {
            HelloApplication.setRoot(fxmlFile);
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to load " + fxmlFile + ". Please ensure the file exists.");
            System.err.println("Navigation failed for " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
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

    public static class Payslip {
        private final String month;
        private final String year;
        private final String name;
        private final String department;
        private final String position;
        private final String joinDate;
        private final double basicSalary;
        private final double workingHours;
        private final double netSalary;
        private final double overtimeHours;

        public Payslip(String month, String year, String name, String department, String position, String joinDate, double basicSalary, double workingHours, double netSalary, double overtimeHours) {
            this.month = month;
            this.year = year;
            this.name = name;
            this.department = department != null ? department : "N/A";
            this.position = position != null ? position : "N/A";
            this.joinDate = joinDate != null ? joinDate : "N/A";
            this.basicSalary = basicSalary;
            this.workingHours = workingHours;
            this.netSalary = netSalary;
            this.overtimeHours = overtimeHours;
        }

        public String getMonth() { return month; }
        public String getYear() { return year; }
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public String getPosition() { return position; }
        public String getJoinDate() { return joinDate; }
        public double getBasicSalary() { return basicSalary; }
        public double getWorkingHours() { return workingHours; }
        public double getNetSalary() { return netSalary; }
        public double getOvertimeHours() { return overtimeHours; }
    }
}