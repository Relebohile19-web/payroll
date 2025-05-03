package com.example.payroll_management;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Cursor;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Employee_payslipController {

    @FXML private TableColumn<PayslipData, String> Action, Department, EmployeeId, NetSalary, Position, name;
    @FXML private Label dashboard, myprofile, payslip, payroll, payslipsTitle, month;
    @FXML private TableView<PayslipData> Table;
    @FXML private ChoiceBox<String> checkbox;
    @FXML private Button logout;

    private ObservableList<PayslipData> payslipDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up navigation for labels
        setupNavigation(dashboard, "employee_dashboard.fxml");
        setupNavigation(myprofile, "employee_profile.fxml");
        setupNavigation(payslip, "employee_payslip.fxml"); // Current page

        // Set up logout button
        if (logout != null) {
            logout.setOnAction(event -> {
                loginController.loggedInEmployeeId = null; // Clear session data
                navigateTo("hello-view.fxml");
            });
        }

        // Initialize ChoiceBox with months
        if (checkbox != null) {
            checkbox.getItems().addAll(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
            );
            checkbox.setValue("January");
            checkbox.setOnAction(event -> loadPayslipData());
        }

        // Set up table columns
        EmployeeId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeId()));
        name.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        Department.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
        Position.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        NetSalary.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNetSalary()));
        Action.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View Payslip");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    viewButton.setOnAction(event -> {
                        PayslipData data = getTableView().getItems().get(getIndex());
                        generatePayslip(data);
                    });
                    setGraphic(viewButton);
                }
            }
        });

        // Load initial payslip data
        loadPayslipData();
    }

    private void setupNavigation(Label label, String fxmlFile) {
        if (label != null) {
            label.setCursor(Cursor.HAND);
            label.setOnMouseClicked(event -> navigateTo(fxmlFile));
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
            showErrorAlert("Navigation Error", "Failed to load: " + fxmlFile + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPayslipData() {
        payslipDataList.clear();
        String selectedMonth = checkbox != null ? checkbox.getValue() : "January";
        String employeeId = loginController.loggedInEmployeeId != null ? loginController.loggedInEmployeeId : "E001"; // Fallback to E001 if not logged in

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.employee_id, e.name, e.department, e.position, p.net_salary " +
                    "FROM payslips p " +
                    "JOIN employees e ON p.employee_id = e.employee_id " +
                    "WHERE p.employee_id = ? AND p.month = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, employeeId);
            pstmt.setString(2, selectedMonth);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String empId = rs.getString("employee_id");
                String empName = rs.getString("name");
                String dept = rs.getString("department");
                String pos = rs.getString("position");
                String netSalary = String.format("%.2f", rs.getDouble("net_salary"));

                payslipDataList.add(new PayslipData(empId, empName, dept, pos, netSalary));
            }
            Table.setItems(payslipDataList);

            if (payslipDataList.isEmpty()) {
                showErrorAlert("Payslip Not Found", "No payslip found for " + selectedMonth);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load payslip data: " + e.getMessage());
        }
    }

    private void generatePayslip(PayslipData data) {
        String selectedMonth = checkbox.getValue();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.*, e.name, e.department, e.position " +
                    "FROM payslips p " +
                    "JOIN employees e ON p.employee_id = e.employee_id " +
                    "WHERE p.employee_id = ? AND p.month = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, data.getEmployeeId());
            stmt.setString(2, selectedMonth);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Fetch payslip data
                double basicSalary = rs.getDouble("basic_salary");
                double workingHours = rs.getDouble("working_hours");
                double overtimeHours = rs.getDouble("overtime_hours");
                double overtimePay = rs.getDouble("overtime_pay");
                double taxDeduction = rs.getDouble("tax_deduction");
                double insuranceDeduction = rs.getDouble("insurance_deduction");
                double otherDeductions = rs.getDouble("other_deductions");
                double netSalary = rs.getDouble("net_salary");
                double grossSalary = basicSalary + overtimePay;
                double hourlyRate = workingHours != 0 ? basicSalary / workingHours : 0;

                // Create payslip dialog
                Stage dialog = new Stage();
                dialog.setTitle("Payslip for " + data.getName() + " - " + selectedMonth);

                VBox vbox = new VBox(10);
                vbox.setPrefWidth(450);
                vbox.setPrefHeight(500);
                vbox.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9; -fx-border-color: #ccc; -fx-border-width: 1;");

                Label title = new Label("Payslip for " + selectedMonth + " " + rs.getInt("year"));
                title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #333;");

                Label companyHeader = new Label("Payroll Management System");
                companyHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #555;");

                Label empDetails = new Label(
                        "Employee ID: " + data.getEmployeeId() + "\n" +
                                "Name: " + data.getName() + "\n" +
                                "Department: " + data.getDepartment() + "\n" +
                                "Position: " + data.getPosition()
                );
                empDetails.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");

                Label earningsHeader = new Label("Earnings");
                earningsHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 10 0 5 0;");

                Label earningsDetails = new Label(
                        "Basic Salary: R" + String.format("%.2f", basicSalary) + "\n" +
                                "Working Hours: " + String.format("%.1f", workingHours) + " hrs\n" +
                                "Hourly Rate: R" + String.format("%.2f", hourlyRate) + "/hr\n" +
                                "Overtime Hours: " + String.format("%.1f", overtimeHours) + " hrs\n" +
                                "Overtime Pay (1.5x): R" + String.format("%.2f", overtimePay) + "\n" +
                                "Gross Salary: R" + String.format("%.2f", grossSalary)
                );
                earningsDetails.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");

                Label deductionsHeader = new Label("Deductions");
                deductionsHeader.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 10 0 5 0;");

                Label deductionsDetails = new Label(
                        "Tax: -R" + String.format("%.2f", taxDeduction) + "\n" +
                                "Insurance: -R" + String.format("%.2f", insuranceDeduction) + "\n" +
                                "Other Deductions: -R" + String.format("%.2f", otherDeductions) + "\n" +
                                "Total Deductions: -R" + String.format("%.2f", (taxDeduction + insuranceDeduction + otherDeductions))
                );
                deductionsDetails.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");

                Label netSalaryLabel = new Label("Net Salary: R" + String.format("%.2f", netSalary));
                netSalaryLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-padding: 10 0 0 0;");

                // Add buttons for printing and closing
                Button printButton = new Button("Print");
                printButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14; -fx-margin: 0 10 0 0;");
                printButton.setOnAction(event -> printPayslip(vbox));

                Button closeButton = new Button("Close");
                closeButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14;");
                closeButton.setOnAction(event -> dialog.close());

                // Add buttons to an HBox for better layout
                javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
                buttonBox.getChildren().addAll(printButton, closeButton);

                vbox.getChildren().addAll(
                        companyHeader,
                        new Label(""), // Spacer
                        title,
                        new Label(""), // Spacer
                        empDetails,
                        new Label(""), // Spacer
                        earningsHeader,
                        earningsDetails,
                        new Label(""), // Spacer
                        deductionsHeader,
                        deductionsDetails,
                        new Label(""), // Spacer
                        netSalaryLabel,
                        new Label(""), // Spacer
                        buttonBox
                );

                dialog.getScene().setRoot(vbox);
                dialog.show();
            } else {
                showErrorAlert("Payslip Not Found", "No payslip found for " + selectedMonth);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to generate payslip: " + e.getMessage());
        }
    }

    private void printPayslip(VBox payslipContent) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            boolean proceed = printerJob.showPrintDialog(null);
            if (proceed) {
                payslipContent.setScaleX(0.8);
                payslipContent.setScaleY(0.8);

                boolean success = printerJob.printPage(payslipContent);
                if (success) {
                    printerJob.endJob();
                    showErrorAlert("Print Success", "Payslip printed successfully.");
                } else {
                    showErrorAlert("Print Error", "Failed to print the payslip.");
                }

                payslipContent.setScaleX(1.0);
                payslipContent.setScaleY(1.0);
            } else {
                printerJob.endJob();
            }
        } else {
            showErrorAlert("Print Error", "No printer available.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PayslipData {
        private final String employeeId;
        private final String name;
        private final String department;
        private final String position;
        private final String netSalary;

        public PayslipData(String employeeId, String name, String department, String position, String netSalary) {
            this.employeeId = employeeId;
            this.name = name;
            this.department = department;
            this.position = position;
            this.netSalary = netSalary;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public String getPosition() {
            return position;
        }

        public String getNetSalary() {
            return netSalary;
        }
    }
}