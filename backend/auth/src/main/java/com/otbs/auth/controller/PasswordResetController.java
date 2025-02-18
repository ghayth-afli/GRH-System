package com.otbs.auth.controller;

import com.otbs.auth.dto.ForgotPasswordRequest;
import com.otbs.auth.dto.MessageResponse;
import com.otbs.auth.dto.ResetPasswordRequest;
import com.otbs.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetTokenForUser(request.email());
        return ResponseEntity.ok().body(new MessageResponse("Password reset token sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, @RequestParam("token") String token) {
        passwordResetService.resetPassword(token, request.password());
        return ResponseEntity.ok().body(new MessageResponse("Password reset successfully"));
    }
}
