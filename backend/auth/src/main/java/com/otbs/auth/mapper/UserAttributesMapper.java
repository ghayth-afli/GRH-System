package com.otbs.auth.mapper;


import com.otbs.auth.model.User;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;


public class UserAttributesMapper implements AttributesMapper<User> {

    @Override
    public User mapFromAttributes(Attributes attributes) throws NamingException {
        User user = new User();
        user.setUsername(attributes.get("sAMAccountName").get().toString());
        user.setAuthorities(attributes.get("memberOf").get().toString().split(",")[0].split("=")[1]);
        return user;
    }
}
