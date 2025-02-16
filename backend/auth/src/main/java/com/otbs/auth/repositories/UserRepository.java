package com.otbs.auth.repositories;

import com.otbs.auth.model.LdapUser;
import com.otbs.auth.model.User;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends LdapRepository<LdapUser> {
}
