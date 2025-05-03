module com.example.payroll_management {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.payroll_management to javafx.fxml;
    exports com.example.payroll_management;
}