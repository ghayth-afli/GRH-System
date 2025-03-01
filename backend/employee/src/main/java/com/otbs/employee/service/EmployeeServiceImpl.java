package com.otbs.employee.service;

import com.otbs.employee.exception.EmployeeNotFoundException;
import com.otbs.employee.mapper.UserAttributesMapper;
import com.otbs.employee.model.Employee;
import com.otbs.employee.model.LdapUser;
import com.otbs.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserAttributesMapper userAttributesMapper;


    @Override
    public Employee getEmployeeByDn(Name dn) {
        return employeeRepository.findById(dn).map(userAttributesMapper).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email).map(userAttributesMapper).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(userAttributesMapper) // Use the mapper
                .filter(employee -> !employee.getDepartment().equals("Unknown")
                        && !employee.getDepartment().equals("Domain Controllers"))
                .toList();
    }
}
