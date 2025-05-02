package com.otbs.auth.mapper;

import com.otbs.auth.model.User;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public class UserAttributesMapper implements AttributesMapper<User> {

    @Override
    public User mapFromAttributes(Attributes attributes) throws NamingException {
        User user = new User();
        user.setUsername(getAttribute(attributes, "sAMAccountName"));
        user.setEmail(getAttribute(attributes, "mail"));
        user.setAuthorities(getFirstGroup(attributes));
        user.setDn(getAttribute(attributes, "distinguishedName"));
        return user;
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        return attributes.get(attributeName).get().toString();
    }

    private String getFirstGroup(Attributes attributes) throws NamingException {
        return attributes.get("memberOf").get().toString().split(",")[0].split("=")[1];
    }
}