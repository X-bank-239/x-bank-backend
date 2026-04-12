package com.example.xbankbackend.services;

import com.example.xbankbackend.repositories.VerificationCodesRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class VerificationService {

    private VerificationCodesRepository verificationCodesRepository;

    public boolean verifyCode(UUID stateId, String code) {
        if (!verificationCodesRepository.exists(stateId)) {
            return false;
        }
        String realCode = verificationCodesRepository.getCode(stateId);
        if (!BCrypt.checkpw(code, realCode)) {
            return false;
        }
        verificationCodesRepository.setUsed(stateId);
        return true;
    }
}
