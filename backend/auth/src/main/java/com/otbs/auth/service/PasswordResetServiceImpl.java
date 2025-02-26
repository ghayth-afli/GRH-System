package com.otbs.auth.service;

import com.otbs.auth.exception.UserNotFoundException;
import com.otbs.auth.model.LdapUser;
import com.otbs.auth.repositories.PasswordResetTokenRepository;
import com.otbs.auth.repositories.UserRepository;
import com.otbs.auth.exception.InvalidTokenException;
import com.otbs.auth.exception.TokenExpiredException;
import com.otbs.auth.model.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LdapTemplate ldapTemplate;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void createPasswordResetTokenForUser(String email) {
        LdapUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(900);

        passwordResetTokenRepository.findByEmail(email).ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken passwordResetToken = new PasswordResetToken(user.getDn(), token, email, expiryDate);
        passwordResetTokenRepository.save(passwordResetToken);

        String resetPasswordLink = "http://localhost:4200/auth/reset-password?token=" + token;
        emailService.sendEmail(email, "Password Reset Link: " + resetPasswordLink);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token expired");
        }

        LdapUser user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update the password and save via repository
        ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd",
                passwordEncoder.encode(newPassword).getBytes(StandardCharsets.UTF_16LE)));

        ldapTemplate.modifyAttributes(user.getDn(), new ModificationItem[]{item});


        // Delete the token
        passwordResetTokenRepository.delete(resetToken);
    }
}

