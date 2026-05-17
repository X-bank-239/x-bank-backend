package com.example.xbankbackend.services;

import com.example.xbankbackend.repositories.VerificationCodesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationService")
class VerificationServiceTest {

    @Mock
    private VerificationCodesRepository verificationCodesRepository;

    @InjectMocks
    private VerificationService verificationService;

    @Nested
    @DisplayName("verifyCode")
    class VerifyCodeTests {

        @Test
        void shouldReturnFalse_WhenCodeNotExists() {
            UUID stateId = UUID.randomUUID();

            when(verificationCodesRepository.exists(stateId)).thenReturn(false);

            boolean result = verificationService.verifyCode(stateId, "123456");

            assertThat(result).isFalse();
            verify(verificationCodesRepository, never()).setUsed(any());
        }

        @Test
        void shouldReturnFalse_WhenCodeIsInvalid() {
            UUID stateId = UUID.randomUUID();
            String correctCode = "123456";
            String incorrectCode = "654321";
            String hashedCode = BCrypt.hashpw(correctCode, BCrypt.gensalt());

            when(verificationCodesRepository.exists(stateId)).thenReturn(true);
            when(verificationCodesRepository.getCode(stateId)).thenReturn(hashedCode);

            boolean result = verificationService.verifyCode(stateId, incorrectCode);

            assertThat(result).isFalse();
            verify(verificationCodesRepository, never()).setUsed(any());
        }

        @Test
        void shouldReturnTrue_WhenCodeIsValid() {
            UUID stateId = UUID.randomUUID();
            String correctCode = "123456";
            String hashedCode = BCrypt.hashpw(correctCode, BCrypt.gensalt());

            when(verificationCodesRepository.exists(stateId)).thenReturn(true);
            when(verificationCodesRepository.getCode(stateId)).thenReturn(hashedCode);

            boolean result = verificationService.verifyCode(stateId, correctCode);

            assertThat(result).isTrue();
            verify(verificationCodesRepository).setUsed(stateId);
        }

        @Test
        void shouldMarkCodeAsUsed_WhenVerificationSucceeds() {
            UUID stateId = UUID.randomUUID();
            String correctCode = "123456";
            String hashedCode = BCrypt.hashpw(correctCode, BCrypt.gensalt());

            when(verificationCodesRepository.exists(stateId)).thenReturn(true);
            when(verificationCodesRepository.getCode(stateId)).thenReturn(hashedCode);

            verificationService.verifyCode(stateId, correctCode);

            verify(verificationCodesRepository).setUsed(stateId);
        }
    }
}
