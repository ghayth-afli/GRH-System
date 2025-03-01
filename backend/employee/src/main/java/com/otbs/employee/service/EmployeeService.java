package com.otbs.employee.service;

import com.otbs.employee.model.Employee;
import com.otbs.employee.model.LdapUser;

import javax.naming.Name;
import java.util.List;

public interface EmployeeService {
    public Employee getEmployeeByDn(Name dn);
    public Employee getEmployeeByEmail(String email);
    public List<Employee> getAllEmployees();
}
