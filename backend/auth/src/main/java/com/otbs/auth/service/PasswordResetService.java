package com.otbs.auth.service;

import com.otbs.auth.model.PasswordResetToken;
import com.otbs.auth.model.User;

public interface PasswordResetService {
    void createPasswordResetTokenForUser(User user, String token);
    PasswordResetToken getPasswordResetToken(String token);
    void deletePasswordResetToken(PasswordResetToken passwordResetToken);
}
