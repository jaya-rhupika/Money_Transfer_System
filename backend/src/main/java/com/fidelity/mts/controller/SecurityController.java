package com.fidelity.mts.controller;

import com.fidelity.mts.entity.UserCredentials;
import com.fidelity.mts.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
