package com.otbs.auth.service;

import com.otbs.auth.model.PasswordResetToken;
import com.otbs.auth.model.User;
import com.otbs.auth.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
    }

    @Override
    public PasswordResetToken getPasswordResetToken(String token) {
        return null;
    }

    @Override
    public void deletePasswordResetToken(PasswordResetToken passwordResetToken) {

    }
}
