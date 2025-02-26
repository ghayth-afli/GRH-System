package com.otbs.auth.repositories;

import com.otbs.auth.model.LdapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.ldap.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends LdapRepository<LdapUser> {
    @Query("(mail={0})")
    Optional<LdapUser> findByEmail(String email);
}
