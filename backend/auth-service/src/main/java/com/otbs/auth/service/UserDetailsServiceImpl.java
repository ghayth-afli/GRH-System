package com.otbs.auth.service;

import com.otbs.feign.client.EmployeeClient;
import com.otbs.feign.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeClient employeeClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            EmployeeResponse user = employeeClient.getEmployeeByUsername(username);
            return UserDetailsImpl.build(user);
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
    }
}