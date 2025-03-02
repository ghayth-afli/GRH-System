package com.otbs.auth.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken,
        long accessExpiration,
        long refreshExpiration
) {}