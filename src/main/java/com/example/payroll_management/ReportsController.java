package com.example.payroll_management;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ReportsController {

    @FXML
    private AnchorPane Anchorpane;

    @FXML
    private Label AverageSalary;

    @FXML
    private Label Dashboard;

    @FXML
    private Button ExportCSV;

    @FXML
    private Button ExportPDF;

    @FXML
    private Label Payrollreports;

    @FXML
    private Label Payrollreports1;

    @FXML
    private Label analyze;

    @FXML
    private Label calculator;

    @FXML
    private Button chartView;

    @FXML
    private ChoiceBox<?> choicebox1;

    @FXML
    private ChoiceBox<?> choicebox2;

    @FXML
    private Label employees;

    @FXML
    private Label frame;

    @FXML
    private Button logout;

    @FXML
    private Label payroll;

    @FXML
    private Label payslip;

    @FXML
    private Label reports;

    @FXML
    private Button tableview;

    @FXML
    private Label totalGrossPayroll;

    @FXML
    private Label totalNetPayroll;

    @FXML
    private Label type;

    @FXML
    public void initialize() {
        // Logout functionality
        if (logout != null) {
            logout.setOnAction(event -> {
                try {
                    HelloApplication.setRoot("hello-view.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Navigation to Dashboard
        if (Dashboard != null) {
            Dashboard.setOnMouseClicked(event -> {
                try {
                    HelloApplication.setRoot("admin_dashboard.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Navigation to Employees
        if (employees != null) {
            employees.setOnMouseClicked(event -> {
                try {
                    HelloApplication.setRoot("employee.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}