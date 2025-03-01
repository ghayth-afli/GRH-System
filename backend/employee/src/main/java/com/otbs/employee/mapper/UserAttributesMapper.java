package com.otbs.employee.mapper;


import com.otbs.employee.model.Employee;
import com.otbs.employee.model.LdapUser;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserAttributesMapper implements Function<LdapUser, Employee> {

    @Override
    public Employee apply(LdapUser ldapUser) {
        Employee employee = new Employee();
        employee.setId(ldapUser.getDn());
        employee.setUsername(ldapUser.getUsername());
        employee.setFirstName(ldapUser.getFirstName());
        employee.setLastName(ldapUser.getLastName());
        employee.setEmail(ldapUser.getEmail());

        // Extract department safely
        String department = extractDepartment(ldapUser.getDn().toString());
        employee.setDepartment(department);

        // Extract role safely
        String role = extractRole(ldapUser.getGroups().toString());
        employee.setRole(role);

        return employee;
    }

    private String extractDepartment(String dn) {
        try {
            String[] dnParts = dn.split(",");
            for (String part : dnParts) {
                if (part.startsWith("OU=")) {
                    return part.split("=")[1];
                }
            }
        } catch (Exception e) {
            return "Unknown";
        }
        return "Unknown";
    }

    private String extractRole(String groups) {
        try {
            String[] groupParts = groups.split(",");
            if (groupParts.length > 0) {
                return groupParts[0].split("=")[1];
            }
        } catch (Exception e) {
            return "Unknown";
        }
        return "Unknown";
    }
}
