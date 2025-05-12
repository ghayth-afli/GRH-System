package com.otbs.auth.controller;

import com.otbs.auth.dto.MessageResponseDTO;
import com.otbs.auth.dto.ForgotPasswordRequestDTO;
import com.otbs.auth.dto.ResetPasswordRequestDTO;
import com.otbs.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.createPasswordResetTokenForUser(request.email());
        return ResponseEntity.ok(new MessageResponseDTO("Password reset token sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@RequestBody ResetPasswordRequestDTO request, @RequestParam("token") String token) {
        passwordResetService.resetPassword(token, request.password());
        return ResponseEntity.ok(new MessageResponseDTO("Password reset successfully"));
    }
}