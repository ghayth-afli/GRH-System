package com.otbs.auth.service;

import com.otbs.auth.exception.UserException;
import com.otbs.auth.repositories.PasswordResetTokenRepository;
import com.otbs.auth.exception.TokenException;
import com.otbs.auth.model.PasswordResetToken;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
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

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LdapTemplate ldapTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmployeeClient employeeClient;
    private final AuthNotificationService authNotificationService;

    @Override
    public void createPasswordResetTokenForUser(String email) {
        try{
            EmployeeResponse user = employeeClient.getEmployeeByEmail(email);
            if (user == null) {
                throw new UserException("User not found");
            }

            String token = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plusSeconds(900);

            passwordResetTokenRepository.findByEmail(email).ifPresent(passwordResetTokenRepository::delete);

            PasswordResetToken passwordResetToken = new PasswordResetToken(user.id(), token, email, expiryDate);
            passwordResetTokenRepository.save(passwordResetToken);

            String resetPasswordLink = "http://localhost:4200/auth/reset-password?token=" + token;
            authNotificationService.sendMailNotification(email, "Reset Password", "Password Reset Link: " + resetPasswordLink);
        }
        catch (RuntimeException e) {
            throw new UserException("Error creating password reset token");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenException("Token expired");
        }

        try{
            EmployeeResponse user = employeeClient.getEmployeeByEmail(resetToken.getEmail());

            ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd",
                    passwordEncoder.encode(newPassword).getBytes(StandardCharsets.UTF_16LE)));
            ldapTemplate.modifyAttributes(user.id(), new ModificationItem[]{item});

            passwordResetTokenRepository.delete(resetToken);
        }
        catch (RuntimeException e) {
            throw new UserException("Error resetting password");
        }
    }
}