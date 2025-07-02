package com.otbs.auth.service;

import com.otbs.auth.dto.AuthRequestDTO;
import com.otbs.auth.dto.JwtResponseDTO;
import com.otbs.auth.dto.RefreshRequestDTO;
import com.otbs.auth.exception.TokenException;
import com.otbs.auth.exception.UserException;
import com.otbs.auth.util.JwtUtils;
import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserClient userClient;

    public JwtResponseDTO authenticateUser(AuthRequestDTO authRequestDTO) {
        try {
            log.info("Authenticating user: {}", authRequestDTO.username());
            UserResponse user = userClient.getUserByUsername(authRequestDTO.username());
            log.info("User found: {}", user);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.username(), authRequestDTO.password()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = List.of(user.role());
            log.info("User roles: {}", roles);

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
            UserResponse user = userClient.getUserByUsername(username);
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
            throw new UserException("Unauthorized: User not found");
        }

    }
}
