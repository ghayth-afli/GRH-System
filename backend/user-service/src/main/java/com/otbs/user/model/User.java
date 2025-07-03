package com.otbs.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "picture")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    @Column(nullable = true)
    private String gender;
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
    @Column(nullable = true)
    private String birthDate;
}
