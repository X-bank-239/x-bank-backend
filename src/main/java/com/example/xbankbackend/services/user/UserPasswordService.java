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
            throw new BadCredentialsException("Старый пароль не соответствует текущему паролю");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Новый пароль не может совпадать со старым");
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
