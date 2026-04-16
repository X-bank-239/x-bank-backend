package com.example.xbankbackend.services.user;

import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserGivesIncorrectEmail;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidationService")
class UserValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidationService service;

    @Nested
    @DisplayName("validateEmail")
    class ValidateEmailTests {

        @Test
        void shouldNotThrow_WhenEmailIsValid() {
            assertThatNoException().isThrownBy(() -> service.validateEmail("test@xbank.ru"));
        }

        @Test
        void shouldThrowUserGivesIncorrectEmail_WhenEmailIsInvalid() {
            assertThatThrownBy(() -> service.validateEmail("invalid-email"))
                    .isInstanceOf(UserGivesIncorrectEmail.class);
        }
    }

    @Nested
    @DisplayName("validateAge")
    class ValidateAgeTests {

        @Test
        void shouldNotThrow_WhenAgeIsValid() {
            Date birthdate = getDateYearsAgo(25);

            assertThatNoException().isThrownBy(() -> service.validateAge(birthdate));
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenUserIsTooYoung() {
            Date birthdate = getDateYearsAgo(5);

            assertThatThrownBy(() -> service.validateAge(birthdate))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenUserIsTooOld() {
            Date birthdate = getDateYearsAgo(120);

            assertThatThrownBy(() -> service.validateAge(birthdate))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private Date getDateYearsAgo(int years) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -years);
            return cal.getTime();
        }
    }

    @Nested
    @DisplayName("validateUserExists")
    class ValidateUserExistsTests {

        @Test
        void shouldNotThrow_WhenUserExists() {
            UUID userId = UUID.randomUUID();

            when(userRepository.exists(userId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateUserExists(userId));
        }

        @Test
        void shouldThrowUserNotFoundException_WhenUserNotExists() {
            UUID userId = UUID.randomUUID();

            when(userRepository.exists(userId)).thenReturn(false);

            assertThatThrownBy(() -> service.validateUserExists(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateUserExistsByEmail")
    class ValidateUserExistsByEmailTests {

        @Test
        void shouldNotThrow_WhenUserExists() {
            when(userRepository.existsByEmail("test@xbank.ru")).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.validateUserExistsByEmail("test@xbank.ru"));
        }

        @Test
        void shouldThrowUserNotFoundException_WhenUserNotExists() {
            when(userRepository.existsByEmail("test@xbank.ru")).thenReturn(false);

            assertThatThrownBy(() -> service.validateUserExistsByEmail("test@xbank.ru"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void shouldThrowUserGivesIncorrectEmail_WhenFormatInvalid() {
            assertThatThrownBy(() -> service.validateUserExistsByEmail("bad-email"))
                    .isInstanceOf(UserGivesIncorrectEmail.class);
        }
    }

    @Nested
    @DisplayName("validateEmailIsUnique")
    class ValidateEmailIsUniqueTests {

        @Test
        void shouldNotThrow_WhenEmailIsUnique() {
            when(userRepository.existsByEmail("new@xbank.ru")).thenReturn(false);

            assertThatNoException().isThrownBy(() -> service.validateEmailIsUnique("new@xbank.ru"));
        }

        @Test
        void shouldThrowUserAlreadyExistsException_WhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("exist@xbank.ru")).thenReturn(true);

            assertThatThrownBy(() -> service.validateEmailIsUnique("exist@xbank.ru"))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }
    }
}
