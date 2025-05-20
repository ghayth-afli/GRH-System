package com.otbs.auth.service;

import com.otbs.auth.dto.AuthRequestDTO;
import com.otbs.auth.dto.JwtResponseDTO;
import com.otbs.auth.dto.RefreshRequestDTO;
import com.otbs.auth.exception.TokenException;
import com.otbs.auth.exception.UserException;
import com.otbs.auth.util.JwtUtils;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmployeeClient employeeClient;

    public JwtResponseDTO authenticateUser(AuthRequestDTO authRequestDTO) {
        try {
            EmployeeResponse user = employeeClient.getEmployeeByUsername(authRequestDTO.username());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.username(), authRequestDTO.password()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String accessToken = jwtUtils.generateAccessToken(authRequestDTO.username(), roles);

            return new JwtResponseDTO(
                    accessToken,
                    jwtUtils.generateRefreshToken(authRequestDTO.username(), roles),
                    jwtUtils.getAccessExpirationMs(),
                    jwtUtils.getRefreshExpirationMs(),
                    user
            );
        }
        catch (RuntimeException e) {
            throw new UserException("Unauthorized: Bad credentials");
        }
    }

    public JwtResponseDTO refreshToken(RefreshRequestDTO refreshRequestDTO) {
        if (!jwtUtils.validateRefreshToken(refreshRequestDTO.refreshToken())) {
            throw new TokenException("Unauthorized: Invalid refresh token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshRequestDTO.refreshToken());

        try {
            EmployeeResponse user = employeeClient.getEmployeeByUsername(username);
            List<String> roles = List.of(user.role());
            return new JwtResponseDTO(
                    jwtUtils.generateAccessToken(username, roles),
                    jwtUtils.generateRefreshToken(username, roles),
                    jwtUtils.getAccessExpirationMs(),
                    jwtUtils.getRefreshExpirationMs(),
                    user
            );
        }
        catch (RuntimeException e) {
            throw new UserException("Unauthorized: Employee not found");
        }

    }
}
