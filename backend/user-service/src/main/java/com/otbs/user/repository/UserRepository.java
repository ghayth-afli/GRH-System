package com.otbs.user.repository;

import com.otbs.user.model.LdapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.ldap.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends LdapRepository<LdapUser> {

    @Query("(mail={0})")
    Optional<LdapUser> findByEmail(String email);

    @Query("(sAMAccountName={0})")
    Optional<LdapUser> findByUsername(String username);

}