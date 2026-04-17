package com.example.xbankbackend.services.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPasswordService")
class UserPasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserPasswordService service;

    @Nested
    @DisplayName("validatePasswordChange")
    class ValidatePasswordChangeTests {

        @Test
        void shouldNotThrow_WhenOldPasswordMatchesAndNewIsDifferent() {
            when(passwordEncoder.matches("old", "hashedOld")).thenReturn(true);

            assertThatNoException().isThrownBy(() ->
                    service.validatePasswordChange("old", "new", "hashedOld"));
        }

        @Test
        void shouldThrowBadCredentialsException_WhenOldPasswordDoesNotMatch() {
            when(passwordEncoder.matches("wrong", "hashedOld")).thenReturn(false);

            assertThatThrownBy(() ->
                    service.validatePasswordChange("wrong", "new", "hashedOld"))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenPasswordsAreSame() {
            when(passwordEncoder.matches("same", "hashedSame")).thenReturn(true);

            assertThatThrownBy(() ->
                    service.validatePasswordChange("same", "same", "hashedSame"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("matches")
    class MatchesTests {

        @Test
        void shouldReturnTrue_WhenPasswordMatches() {
            when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);

            assertThat(service.matches("pass", "hashed")).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenPasswordDoesNotMatch() {
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

            assertThat(service.matches("wrong", "hashed")).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenStoredHashIsNull() {
            assertThat(service.matches("pass", null)).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenStoredHashIsBlank() {
            assertThat(service.matches("pass", "   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("encodePassword")
    class EncodePasswordTests {

        @Test
        void shouldDelegateToPasswordEncoder() {
            when(passwordEncoder.encode("plain")).thenReturn("hashed");

            String result = service.encodePassword("plain");

            assertThat(result).isEqualTo("hashed");
            verify(passwordEncoder).encode("plain");
        }
    }
}
