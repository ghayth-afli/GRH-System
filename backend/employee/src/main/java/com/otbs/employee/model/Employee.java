package com.otbs.employee.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.otbs.employee.util.LdapNameSerializer;
import lombok.*;

import javax.naming.Name;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @JsonSerialize(using = LdapNameSerializer.class)
    private Name id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
}
