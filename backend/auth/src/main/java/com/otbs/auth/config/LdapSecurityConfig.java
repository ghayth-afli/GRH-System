package com.otbs.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import java.util.Collections;

@Configuration
public class LdapSecurityConfig {

    /*@Bean
    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) {
        LdapAuthenticationProviderConfigurer<WebSecurity> configurer = new LdapAuthenticationProviderConfigurer<>(
                new DefaultSpringSecurityContextSource(contextSource)
        );

        return new ProviderManager(
                configurer
                        .userSearchBase("")
                        .userSearchFilter("(sAMAccountName={0})")
                        .groupSearchBase("CN=Users")
                        .groupSearchFilter("(member={0})")
                        .passwordCompare()
                        .passwordEncoder(new LdapShaPasswordEncoder())
                        .passwordAttribute("userPassword")
                        .and()
                        .toAuthenticationProvider()
        );
    }*/

    @Bean
    public AuthenticationManager authenticationManager() {
        ActiveDirectoryLdapAuthenticationProvider adProvider =
                new ActiveDirectoryLdapAuthenticationProvider("otbs.local", "ldap://192.168.112.133:389");
        adProvider.setConvertSubErrorCodesToExceptions(true);
        adProvider.setUseAuthenticationRequestCredentials(true);

        return new ProviderManager(Collections.singletonList(adProvider));
    }
}
