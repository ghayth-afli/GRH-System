package com.otbs.auth.service;

import com.otbs.feign.client.EmployeeClient;
import com.otbs.feign.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import com.otbs.auth.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LdapTemplate ldapTemplate;
    private final EmployeeClient employeeClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        EmployeeResponse user = employeeClient.getEmployeeByUsername(username).getBody();
        assert user != null;
        return UserDetailsImpl.build(user);
    }
}
