package com.example.xbankbackend.services.user;

import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.enums.UserRole;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.UserProfileMapper;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private UserPasswordService userPasswordService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void shouldCreateUser_WhenValidationsPass() {
            User input = buildValidUser();
            UserProfileResponse expected = new UserProfileResponse();

            when(userPasswordService.encodePassword("pass")).thenReturn("hashed");
            when(bankAccountRepository.getBankAccounts(any())).thenReturn(List.of());
            when(userRepository.getUser(any())).thenReturn(input);
            when(userProfileMapper.map(any(), any())).thenReturn(expected);

            UserProfileResponse result = userService.create(input);

            assertThat(result).isEqualTo(expected);
            verify(userValidationService).validateEmailIsUnique(input.getEmail());
            verify(userValidationService).validateAge(input.getBirthdate());
            verify(userPasswordService).encodePassword("pass");
            verify(userRepository).create(input);
        }
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfileTests {

        @Test
        void shouldReturnProfile_WhenUserExists() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            UserProfileResponse expected = new UserProfileResponse();

            doNothing().when(userValidationService).validateUserExists(userId);
            when(userRepository.getUser(userId)).thenReturn(user);
            when(bankAccountRepository.getBankAccounts(userId)).thenReturn(List.of());
            when(userProfileMapper.map(user, List.of())).thenReturn(expected);

            UserProfileResponse result = userService.getProfile(userId);

            assertThat(result).isEqualTo(expected);
            verify(userValidationService).validateUserExists(userId);
            verify(userRepository).getUser(userId);
        }
    }

    @Nested
    @DisplayName("getProfileByEmail")
    class GetProfileByEmailTests {

        @Test
        void shouldReturnProfile_WhenEmailExists() {
            String email = "test@xbank.ru";
            User user = new User();
            user.setUserId(UUID.randomUUID());
            UserProfileResponse expected = new UserProfileResponse();

            doNothing().when(userValidationService).validateUserExistsByEmail(email);
            when(userRepository.getUserByEmail(email)).thenReturn(user);
            when(bankAccountRepository.getBankAccounts(user.getUserId())).thenReturn(List.of());
            when(userProfileMapper.map(user, List.of())).thenReturn(expected);

            UserProfileResponse result = userService.getProfileByEmail(email);

            assertThat(result).isEqualTo(expected);
            verify(userValidationService).validateUserExistsByEmail(email);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        void shouldUpdatePassword_WhenValidationsPass() {
            UUID userId = UUID.randomUUID();

            doNothing().when(userValidationService).validateUserExists(userId);
            when(userRepository.getHashedPassword(userId)).thenReturn("oldHash");
            doNothing().when(userPasswordService).validatePasswordChange("old", "new", "oldHash");
            when(userPasswordService.encodePassword("new")).thenReturn("newHash");

            userService.changePassword(userId, "old", "new");

            verify(userValidationService).validateUserExists(userId);
            verify(userPasswordService).validatePasswordChange("old", "new", "oldHash");
            verify(userRepository).updatePassword(userId, "newHash");
        }
    }

    @Nested
    @DisplayName("blockUser")
    class BlockUserTests {

        @Test
        void shouldBlockUser_WhenExists() {
            UUID userId = UUID.randomUUID();

            doNothing().when(userValidationService).validateUserExists(userId);

            userService.blockUser(userId);

            verify(userValidationService).validateUserExists(userId);
            verify(userRepository).block(userId);
        }
    }

    @Nested
    @DisplayName("authenticated")
    class AuthenticatedTests {

        @Test
        void shouldReturnFalse_WhenEmailNotExists() {
            when(userRepository.existsByEmail("test@xbank.ru")).thenReturn(false);

            assertThat(userService.authenticated("test@xbank.ru", "pass")).isFalse();
        }

        @Test
        void shouldReturnTrue_WhenPasswordMatches() {
            User user = new User();
            user.setPassword("hashed");

            when(userRepository.existsByEmail("test@xbank.ru")).thenReturn(true);
            when(userRepository.getUserByEmail("test@xbank.ru")).thenReturn(user);
            when(userPasswordService.matches("pass", "hashed")).thenReturn(true);

            assertThat(userService.authenticated("test@xbank.ru", "pass")).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenPasswordDoesNotMatch() {
            User user = new User();
            user.setPassword("hashed");

            when(userRepository.existsByEmail("test@xbank.ru")).thenReturn(true);
            when(userRepository.getUserByEmail("test@xbank.ru")).thenReturn(user);
            when(userPasswordService.matches("wrong", "hashed")).thenReturn(false);

            assertThat(userService.authenticated("test@xbank.ru", "wrong")).isFalse();
        }
    }

    @Nested
    @DisplayName("generateTokenByEmail")
    class GenerateTokenByEmailTests {

        @Test
        void shouldGenerateToken_WhenUserExists() {
            User user = new User();
            user.setUserId(UUID.randomUUID());
            user.setRole(UserRole.USER);

            when(userRepository.getUserByEmail("test@xbank.ru")).thenReturn(user);
            when(jwtUtil.generateToken(user.getUserId(), "USER")).thenReturn("jwt-token");

            String result = userService.generateTokenByEmail("test@xbank.ru");

            assertThat(result).isEqualTo("jwt-token");
            verify(jwtUtil).generateToken(user.getUserId(), "USER");
        }
    }

    private User buildValidUser() {
        User user = new User();
        user.setEmail("test@xbank.ru");
        user.setPassword("pass");
        user.setBirthdate(getDateYearsAgo(20));
        return user;
    }

    private Date getDateYearsAgo(int years) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -years);
        return cal.getTime();
    }
}
