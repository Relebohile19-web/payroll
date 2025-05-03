package com.example.payroll_management;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class EmployeeController {

    @FXML
    private TableColumn<?, ?> BasicSalary;

    @FXML
    private Label Dashboard;

    @FXML
    private TableColumn<?, ?> EmployeeID;

    @FXML
    private Label EmployeeRecords;

    @FXML
    private TableColumn<?, ?> WorkingHours;

    @FXML
    private Button add;

    @FXML
    private AnchorPane anchorpane;

    @FXML
    private Label calculator;

    @FXML
    private Button delete;

    @FXML
    private TableColumn<?, ?> department;

    @FXML
    private Label employees;

    @FXML
    private Button logout;

    @FXML
    private TableColumn<?, ?> name;

    @FXML
    private Label payroll;

    @FXML
    private Label payslip;

    @FXML
    private TableColumn<?, ?> position;

    @FXML
    private Label reports;

    @FXML
    private TableView<?> table;

    @FXML
    private Button update;

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

        // Navigation to Reports
        if (reports != null) {
            reports.setOnMouseClicked(event -> {
                try {
                    HelloApplication.setRoot("reports.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}