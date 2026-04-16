package com.example.xbankbackend.services.user;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.enums.UserRole;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.mappers.UserProfileMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private BankAccountRepository bankAccountRepository;
    private UserProfileMapper userProfileMapper;
    private BankAccountMapper bankAccountMapper;
    private JwtUtil jwtUtil;

    private final UserValidationService userValidationService;
    private final UserPasswordService userPasswordService;

    public UserProfileResponse create(@Valid User user) {
        userValidationService.validateEmailIsUnique(user.getEmail());
        userValidationService.validateAge(user.getBirthdate());

        String hashedPassword = userPasswordService.encodePassword(user.getPassword());
        user.setPassword(hashedPassword);

        user.setUserId(UUID.randomUUID());
        user.setRole(UserRole.USER);
        user.setActive(Boolean.TRUE);
        userRepository.create(user);

        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(user.getUserId());

        return userProfileMapper.map(userRepository.getUser(user.getUserId()), accounts);
    }

    public UserProfileResponse getProfile(UUID userId) {
        userValidationService.validateUserExists(userId);

        User user = userRepository.getUser(userId);
        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(userId);

        return userProfileMapper.map(user,accounts);
    }

    public List<BankAccountResponse> getAccounts(UUID userId) {
        userValidationService.validateUserExists(userId);

        List<BankAccount> bankAccounts = bankAccountRepository.getBankAccounts(userId);

        return bankAccountMapper.accountsToResponses(bankAccounts);
    }

    public UserProfileResponse getProfileByEmail(String email) {
        userValidationService.validateEmail(email);
        userValidationService.validateUserExistsByEmail(email);

        User user = userRepository.getUserByEmail(email);

        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(user.getUserId());

        return userProfileMapper.map(user, accounts);
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        userValidationService.validateUserExists(userId);
        String hashedPassword = userRepository.getHashedPassword(userId);
        userPasswordService.validatePasswordChange(oldPassword, newPassword, hashedPassword);

        userRepository.updatePassword(userId, userPasswordService.encodePassword(newPassword));
    }

    public void blockUser(UUID userId) {
        userValidationService.validateUserExists(userId);

        userRepository.block(userId);
    }

    public String generateTokenByEmail(String email){
        User user = userRepository.getUserByEmail(email);
        return jwtUtil.generateToken(user.getUserId(), user.getRole().toString());
    }

    public boolean authenticated(String email, String inputPassword){
        if (!userRepository.existsByEmail(email)) {
            return false;
        }

        User user = userRepository.getUserByEmail(email);
        String storedHash = user.getPassword();

        return userPasswordService.matches(inputPassword, storedHash);
    }
}
