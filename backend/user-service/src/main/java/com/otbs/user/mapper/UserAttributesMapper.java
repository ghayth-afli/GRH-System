package com.otbs.user.mapper;

import com.otbs.user.model.User;
import com.otbs.user.model.LdapUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Slf4j
public class UserAttributesMapper implements Function<LdapUser, User> {

    @Override
    public User apply(LdapUser ldapUser) {
        return User.builder()
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
        log.info("Extracting role from groups: {}", groups);
        return groupParts.length > 0 ? groupParts[0].split("=")[1] : "Unknown";
    }
}