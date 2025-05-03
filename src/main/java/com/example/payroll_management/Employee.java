package com.example.payroll_management;

public class Employee {
    private String employeeID;
    private String name;
    private String department;
    private String position;
    private double basicSalary;
    private int workingHours;

    public Employee(String employeeID, String name, String department,
                    String position, double basicSalary, int workingHours) {
        this.employeeID = employeeID;
        this.name = name;
        this.department = department;
        this.position = position;
        this.basicSalary = basicSalary;
        this.workingHours = workingHours;
    }

    // Getters and Setters
    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public int getWorkingHours() { return workingHours; }
    public void setWorkingHours(int workingHours) { this.workingHours = workingHours; }
}