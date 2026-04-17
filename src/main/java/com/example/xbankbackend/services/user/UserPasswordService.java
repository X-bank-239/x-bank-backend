package com.example.xbankbackend.services.user;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserPasswordService {

    private final PasswordEncoder passwordEncoder;

    public void validatePasswordChange(String oldPassword, String newPassword, String hashedPassword) {
        if (!passwordEncoder.matches(oldPassword, hashedPassword)) {
            throw new BadCredentialsException("Old password doesn't match actual password");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password cannot math old one");
        }
    }

    public boolean matches(String inputPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(inputPassword, storedHash);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
