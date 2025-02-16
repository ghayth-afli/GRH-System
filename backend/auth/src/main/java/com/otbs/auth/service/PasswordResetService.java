package com.otbs.auth.service;

import com.otbs.auth.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetService {
    void createPasswordResetTokenForUser(String email);
    void resetPassword(String token, String password);
}
