package com.otbs.employee.repository;

import com.otbs.employee.model.Employee;
import com.otbs.employee.model.LdapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.ldap.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends LdapRepository<LdapUser> {
    @Query("(mail={0})")
    Optional<LdapUser> findByEmail(String email);
}
