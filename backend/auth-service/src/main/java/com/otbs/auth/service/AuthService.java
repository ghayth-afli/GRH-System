package com.otbs.auth.service;

import com.otbs.auth.dto.AuthRequestDTO;
import com.otbs.auth.dto.JwtResponseDTO;
import com.otbs.auth.dto.RefreshRequestDTO;

public interface AuthService {
    JwtResponseDTO authenticateUser(AuthRequestDTO authRequestDTO);
    JwtResponseDTO refreshToken(RefreshRequestDTO refreshRequestDTO);
}
