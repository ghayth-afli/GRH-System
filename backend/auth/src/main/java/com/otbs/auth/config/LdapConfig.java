package com.otbs.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.username}")
    private String managerDn;

    @Value("${spring.ldap.password}")
    private String managerPassword;

    @Value("${spring.ldap.base}")
    private String baseDn;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setUserDn(managerDn);
        contextSource.setPassword(managerPassword);
        contextSource.setBase(baseDn);  // Set base DN here
        contextSource.setReferral("follow");
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate template = new LdapTemplate(contextSource());
        template.setIgnorePartialResultException(true);  // Important for Active Directory
        return template;
    }
}