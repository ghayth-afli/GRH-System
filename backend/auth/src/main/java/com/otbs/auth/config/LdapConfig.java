package com.otbs.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${LDAP_SERVER_SECURE_URL}")
    private String secureLdapUrl;

    @Value("${spring.ldap.username}")
    private String managerDn;

    @Value("${spring.ldap.password}")
    private String managerPassword;

    @Value("${spring.ldap.base}")
    private String baseDn;

    @Bean
    @Primary
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(baseDn);
        contextSource.setUserDn(managerDn);
        contextSource.setPassword(managerPassword);
        contextSource.setReferral("follow");
        return contextSource;
    }

    // Secure LDAPS (port 636)
    @Bean(name = "secureLdapContextSource")
    public LdapContextSource secureLdapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(secureLdapUrl);
        contextSource.setBase(baseDn);
        contextSource.setUserDn(managerDn);
        contextSource.setPassword(managerPassword);
        contextSource.setReferral("follow");
        return contextSource;
    }

    // Regular LDAP template
    @Bean
    @Primary
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }

    // Secure LDAPS template
    @Bean(name = "secureLdapTemplate")
    public LdapTemplate secureLdapTemplate(
            @Qualifier("secureLdapContextSource") LdapContextSource secureLdapContextSource) {
        return new LdapTemplate(secureLdapContextSource);
    }
}