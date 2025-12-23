package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserGivesIncorrectEmail;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.UserProfileMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // create
    @Test
    void create_shouldThrowIfEmailAlreadyExists() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.create(user));
    }

    @Test
    void create_shouldThrowIfBirthdateIsTooOld() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -200);
        Date birthdate = cal.getTime();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.create(user));
    }

    @Test
    void create_shouldThrowIfBirthdateIsTooYoung() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -2);
        Date birthdate = cal.getTime();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.create(user));
    }

    @Test
    void createUser_shouldCreateWithDefaultValues() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        String password = "1122334455";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date birthdate = cal.getTime();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);
        user.setPassword(password);

        when(userRepository.existsByEmail(email)).thenReturn(false);

        userService.create(user);

        assertNotNull(user.getUserId());

        verify(userRepository).create(user);
    }

    // getUserId
    @Test
    void getProfileUser_shouldThrowIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        when(userRepository.exists(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.getProfile(userId));
    }

    @Test
    void getProfileUserByEmail_shouldThrowIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.getProfileByEmail(email));
    }

    @Test
    void getProfileUserByEmail_shouldThrowIfEmailIsIncorrect() {
        UUID userId = UUID.randomUUID();
        String firstName = "Test";
        String lastName = "User";
        String email = "testxbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        assertThrows(UserGivesIncorrectEmail.class, () -> userService.getProfileByEmail(email));
    }

    @Test
    void getUser_shouldGetProfileUserProfileDTO() {
        UUID userId = UUID.randomUUID();
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

        BankAccount account1 = new BankAccount();
        account1.setBalance(BigDecimal.valueOf(100.0));
        account1.setCurrency(CurrencyType.RUB);
        account1.setAccountType(BankAccountType.CREDIT);

        BankAccount account2 = new BankAccount();
        account2.setBalance(BigDecimal.valueOf(5000.0));
        account2.setCurrency(CurrencyType.CNY);
        account2.setAccountType(BankAccountType.DEBIT);

        List<BankAccount> bankAccounts = List.of(account1, account2);

        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.getUser(userId)).thenReturn(user);
        when(bankAccountRepository.getBankAccounts(userId)).thenReturn(bankAccounts);

        UserProfileResponse userProfileResponse = userService.getProfile(userId);

        assertEquals(firstName, userProfileResponse.getFirstName());
        assertEquals(lastName, userProfileResponse.getLastName());
        assertEquals(email, userProfileResponse.getEmail());
        assertEquals(birthdate, userProfileResponse.getBirthdate());

        assertEquals(2, userProfileResponse.getAccounts().size());

        assertEquals(BigDecimal.valueOf(100.0), userProfileResponse.getAccounts().get(0).getAmount());
        assertEquals(CurrencyType.RUB, userProfileResponse.getAccounts().get(0).getCurrency());
        assertEquals(BankAccountType.CREDIT, userProfileResponse.getAccounts().get(0).getAccountType());

        assertEquals(BigDecimal.valueOf(5000.0), userProfileResponse.getAccounts().get(1).getAmount());
        assertEquals(CurrencyType.CNY, userProfileResponse.getAccounts().get(1).getCurrency());
        assertEquals(BankAccountType.DEBIT, userProfileResponse.getAccounts().get(1).getAccountType());
    }
}
