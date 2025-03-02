package com.otbs.auth.service;

import com.otbs.auth.model.User;
import com.otbs.feign.dto.EmployeeResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class UserDetailsImpl  implements UserDetails {
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(EmployeeResponse user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.role()));

        return new UserDetailsImpl(
                user.username(),
                authorities);
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

}
