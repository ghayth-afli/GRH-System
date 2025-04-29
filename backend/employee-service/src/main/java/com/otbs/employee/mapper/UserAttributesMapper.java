package com.otbs.employee.mapper;

import com.otbs.employee.model.Employee;
import com.otbs.employee.model.LdapUser;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserAttributesMapper implements Function<LdapUser, Employee> {

    @Override
    public Employee apply(LdapUser ldapUser) {
        return Employee.builder()
                .id(ldapUser.getDn().toString())
                .username(ldapUser.getUsername())
                .firstName(ldapUser.getFirstName())
                .lastName(ldapUser.getLastName())
                .email(ldapUser.getEmail())
                .department(extractDepartment(ldapUser.getDn().toString()))
                .role(extractRole(ldapUser.getGroups().toString()))
                .build();
    }

    private String extractDepartment(String dn) {
        for (String part : dn.split(",")) {
            if (part.startsWith("OU=")) {
                return part.split("=")[1];
            }
        }
        return "Unknown";
    }

    private String extractRole(String groups) {
        String[] groupParts = groups.split(",");
        return groupParts.length > 0 ? groupParts[0].split("=")[1] : "Unknown";
    }
}