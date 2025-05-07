package com.otbs.employee.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.otbs.employee.util.LdapNameSerializer;
import jakarta.persistence.*;
import lombok.*;

import javax.naming.Name;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "picture")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee {
    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber1;
    @Column(nullable = true)
    private String phoneNumber2;
    private String department;
    private String role;
    @Lob
    @Column(nullable = true)
    private byte[] picture;
    @Column(nullable = true)
    private String pictureType;
    @Column(nullable = true)
    private String jobTitle;
}
