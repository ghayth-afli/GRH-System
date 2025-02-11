package com.otbs.auth.service;

import com.otbs.auth.mapper.UserAttributesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import com.otbs.auth.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public UserDetailsServiceImpl(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = ldapTemplate.search(
                LdapQueryBuilder.query().where("sAMAccountName").is(username),
                new UserAttributesMapper()
        );
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = users.get(0);
        return UserDetailsImpl.build(user);
    }
}
