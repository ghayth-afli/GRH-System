package com.otbs.auth.service;

import com.otbs.auth.exception.InvalidTokenException;
import com.otbs.auth.exception.TokenExpiredException;
import com.otbs.auth.exception.UserNotFoundException;
import com.otbs.auth.model.LdapUser;
import com.otbs.auth.model.PasswordResetToken;
import com.otbs.auth.repositories.PasswordResetTokenRepository;
import com.otbs.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
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
    @Qualifier("secureLdapTemplate")
    private final LdapTemplate secureLdapTemplate;

    @Override
    public void createPasswordResetTokenForUser(String email) {

        LdapUser user = userRepository.findAll().stream()
                .filter(u -> Objects.equals(u.getEmail(), email))
                .findFirst()
                .orElse(null);

        if (user != null) {
            // Generate a random token
            String token = UUID.randomUUID().toString();

            // Set token expiration (e.g., 15 minutes from now)
            Instant expiryDate = Instant.now().plusSeconds(900); // 15 minutes

            // Remove existing token if any
            passwordResetTokenRepository.findByEmail(email).ifPresent(passwordResetTokenRepository::delete);

            PasswordResetToken passwordResetToken = new PasswordResetToken(user.getDn(),token, user.getEmail(), expiryDate);

            passwordResetTokenRepository.save(passwordResetToken);

            // Send email with the token
            String resetPasswordLink = "http://localhost:8080/reset-password?token=" + token;
            String emailBody = "To reset your password, click the link below:\n" + resetPasswordLink;
            emailService.sendEmail(email, emailBody);
        }
        else
            throw new UserNotFoundException("User not found with email: " + email);
    }

    @Override
    public void resetPassword(String token, String password) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken.isPresent()) {
            if (passwordResetToken.get().getExpiryDate().isAfter(Instant.now())) {

                LdapUser user = userRepository.findById(passwordResetToken.get().getDn()).get();

                // Encode the password for AD's unicodePwd
                String newPassword = "\"" + password + "\""; // Surround with quotes
                byte[] encodedPassword;
                try {
                    encodedPassword = newPassword.getBytes("UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encode password", e);
                }

                // Update the user's password using unicodePwd
                ModificationItem[] modificationItems = new ModificationItem[1];
                modificationItems[0] = new ModificationItem(
                        DirContext.REPLACE_ATTRIBUTE,
                        new BasicAttribute("unicodePwd", encodedPassword)
                );

                secureLdapTemplate.modifyAttributes(user.getDn(), modificationItems);

                // Delete the token
                passwordResetTokenRepository.delete(passwordResetToken.get());

            } else {
                throw new TokenExpiredException("Token expired");
            }
        } else {
            throw new InvalidTokenException("Invalid token");
        }
    }
}

