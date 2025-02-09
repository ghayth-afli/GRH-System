package com.otbs.auth.dto;

public record JwtResponse(String token, long expiration) {}