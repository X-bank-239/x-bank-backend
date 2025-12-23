package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserGivesIncorrectEmail;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.UserProfileMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private BankAccountRepository bankAccountRepository;
    private UserProfileMapper userProfileMapper;
    private JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse create(@Valid User user) {
        if (!isValidEmail(user.getEmail())) {
            throw new UserGivesIncorrectEmail(user.getEmail() + " is not a valid email address");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }

        int maxUserAge = 100;
        int minUserAge = 12;

        Date userBirthdate = user.getBirthdate();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -minUserAge);
        Date minDate = cal.getTime();

        if (userBirthdate.after(minDate)) {
            throw new IllegalArgumentException("Birthdate " + userBirthdate + " is too young");
        }

        cal.add(Calendar.YEAR, -(maxUserAge - minUserAge));
        Date maxDate = cal.getTime();

        if (userBirthdate.before(maxDate)) {
            throw new IllegalArgumentException("Birthdate " + userBirthdate + " is too old");
        }

        Argon2PasswordEncoder arg2SpringSecurity = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        String hashedPassword = arg2SpringSecurity.encode(user.getPassword());
        user.setPassword(hashedPassword);

        user.setUserId(UUID.randomUUID());
        userRepository.create(user);

        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(user.getUserId());

        return userProfileMapper.map(userRepository.getUser(user.getUserId()), accounts);
    }

    public UserProfileResponse getProfile(UUID uuid) {
        if (!userRepository.exists(uuid)) {
            throw new UserNotFoundException("User with UUID " + uuid + " doesn't exist");
        }
        User user = userRepository.getUser(uuid);
        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(uuid);

        UserProfileResponse userProfileResponse = new UserProfileResponse().builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .birthdate(user.getBirthdate())
                .accounts(accounts.stream().map(
                        bankAccount -> {
                            BankAccountResponse bankAccountResponse = new BankAccountResponse();
                            bankAccountResponse.setAmount(bankAccount.getBalance());
                            bankAccountResponse.setCurrency(bankAccount.getCurrency());
                            bankAccountResponse.setAccountType(bankAccount.getAccountType());
                            return bankAccountResponse;
                        }
                ).toList())
                .build();

        return userProfileResponse;
    }

    public UserProfileResponse getProfileByEmail(String email) {
        if (!isValidEmail(email)) {
            throw new UserGivesIncorrectEmail("Not a valid email address");
        }
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with Email " + email + " doesn't exist");
        }
        User user = userRepository.getUserByEmail(email);

        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(user.getUserId());

        UserProfileResponse userProfileResponse = userProfileMapper.map(user, accounts);

        return userProfileResponse;
    }
    public String generateTokenByEmail(String email){
        User user = userRepository.getUserByEmail(email);
        return jwtUtil.generateToken(user.getUserId());
    }
    public boolean authenticated(String email, String input_password){
        if (!userRepository.existsByEmail(email)) {
            return false;
        }

        User user = userRepository.getUserByEmail(email);
        String password = userRepository.getHashedPassword(user.getUserId());

        if (password != "" && passwordEncoder.matches(input_password, user.getPassword())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }
}
