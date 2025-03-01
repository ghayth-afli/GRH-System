package com.otbs.auth.controller;

import com.otbs.auth.dto.JwtResponse;
import com.otbs.auth.dto.AuthRequest;
import com.otbs.auth.dto.RefreshRequest;
import com.otbs.auth.mapper.UserAttributesMapper;
import com.otbs.auth.model.User;
import com.otbs.auth.util.JwtUtils;
import com.otbs.clients.employee.EmployeeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LdapTemplate ldapTemplate;
    private final EmployeeClient employeeClient;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.username(),
                        authRequest.password()
                )
        );
        String username = authentication.getName();
        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        ResponseEntity<?> employee = employeeClient.getEmployeeByEmail("khaledt@gmail.com");
        log.info("Employee: {}", employee.getBody());


        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                refreshToken,
                jwtUtils.getAccessExpirationMs(),
                jwtUtils.getRefreshExpirationMs()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();

        if (jwtUtils.validateRefreshToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);

            // Verify user still exists in LDAP
            List<User> users = ldapTemplate.search(
                    LdapQueryBuilder.query().where("sAMAccountName").is(username),
                    new UserAttributesMapper()
            );


            if (!users.isEmpty()) {
                String newAccessToken = jwtUtils.generateAccessToken(username);
                String newRefreshToken = jwtUtils.generateRefreshToken(username);

                return ResponseEntity.ok(new JwtResponse(
                        newAccessToken,
                        newRefreshToken,
                        jwtUtils.getAccessExpirationMs(),
                        jwtUtils.getRefreshExpirationMs()
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}