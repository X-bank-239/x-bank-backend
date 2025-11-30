package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;
    private BankAccountRepository bankAccountRepository;

    public User create(@Valid User user) {
        Optional<User> createdUser = userRepository.create(user);
        if (userRepository.existsByEmail(user.getEmail()) && !createdUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }
        return createdUser.get();
    }

    public UserProfileResponse getProfile(UUID uuid) {
        if (!userRepository.exists(uuid)) {
            throw new UserNotFoundException("User with UUID " + uuid + " doesn't exist");
        }
        User user = userRepository.getUser(uuid);
        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(uuid);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName(user.getFirstName());
        userProfileResponse.setLastName(user.getLastName());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setBirthdate(user.getBirthdate());

        List<BankAccountResponse> accountDTOS = accounts.stream().map(
                bankAccount -> {
                    BankAccountResponse bankAccountResponse = new BankAccountResponse();
                    bankAccountResponse.setAmount(bankAccount.getBalance());
                    bankAccountResponse.setCurrency(bankAccount.getCurrency());
                    bankAccountResponse.setAccountType(bankAccount.getAccountType());
                    return bankAccountResponse;
                }
        ).toList();

        userProfileResponse.setAccounts(accountDTOS);

        return userProfileResponse;
    }
}
