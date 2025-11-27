package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.UserProfileDTO;
import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void createUser_shouldCreateWithDefaultValues() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@xbank.ru";
        Date birthdate = new Date();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setBirthdate(birthdate);

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
        account1.setBalance(100.0f);
        account1.setCurrency(CurrencyType.RUB);
        account1.setAccountType(BankAccountType.CREDIT);

        BankAccount account2 = new BankAccount();
        account2.setBalance(5000.0f);
        account2.setCurrency(CurrencyType.CNY);
        account2.setAccountType(BankAccountType.DEBIT);

        List<BankAccount> bankAccounts = List.of(account1, account2);

        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.getUser(userId)).thenReturn(user);
        when(bankAccountRepository.getBankAccounts(userId)).thenReturn(bankAccounts);

        UserProfileDTO userProfileDTO = userService.getProfile(userId);

        assertEquals(firstName, userProfileDTO.getFirstName());
        assertEquals(lastName, userProfileDTO.getLastName());
        assertEquals(email, userProfileDTO.getEmail());
        assertEquals(birthdate, userProfileDTO.getBirthdate());

        assertEquals(2, userProfileDTO.getAccounts().size());

        assertEquals(100.0f, userProfileDTO.getAccounts().get(0).getAmount());
        assertEquals(CurrencyType.RUB, userProfileDTO.getAccounts().get(0).getCurrency());
        assertEquals(BankAccountType.CREDIT, userProfileDTO.getAccounts().get(0).getAccountType());

        assertEquals(5000.0f, userProfileDTO.getAccounts().get(1).getAmount());
        assertEquals(CurrencyType.CNY, userProfileDTO.getAccounts().get(1).getCurrency());
        assertEquals(BankAccountType.DEBIT, userProfileDTO.getAccounts().get(1).getAccountType());
    }
}
