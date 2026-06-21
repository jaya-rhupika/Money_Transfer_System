package com.fidelity.mts.controller;

import com.fidelity.mts.dto.ChangePasswordRequestDto;
import com.fidelity.mts.entity.UserCredentials;
import com.fidelity.mts.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import java.security.Principal;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class SecurityController {
    
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @GetMapping("/auth")
    public ResponseEntity<?> authenticate(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = principal.getName();
        Optional<UserCredentials> user = userCredentialsRepository.findByUsername(username);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PutMapping("/auth/change-password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody ChangePasswordRequestDto request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = principal.getName();
        Optional<UserCredentials> userOpt = userCredentialsRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        UserCredentials user = userOpt.get();
        if (request.getCurrentPassword() == null || !request.getCurrentPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("New password cannot be empty");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirmation do not match");
        }

        user.setPassword(request.getNewPassword());
        userCredentialsRepository.save(user);

        return ResponseEntity.ok(
            Map.of("message", "Password changed successfully")
        );
    }
}
