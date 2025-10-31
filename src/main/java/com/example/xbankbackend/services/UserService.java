package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.BankAccountDTO;
import com.example.xbankbackend.dtos.UserProfileDTO;
import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public void createUser(User user) {
        if (userRepository.haveEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }
        user.setUserId(UUID.randomUUID());
        userRepository.createUser(user);
    }

    public UserProfileDTO getUser(UUID uuid) {
        if (!userRepository.haveUUID(uuid)) {
            throw new UserNotFoundException("User with UUID " + uuid + " doesn't exist");
        }
        User user = userRepository.getUser(uuid);
        List<BankAccount> accounts = bankAccountRepository.getBankAccounts(uuid);

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setFirstName(user.getFirstName());
        userProfileDTO.setLastName(user.getLastName());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setBirthdate(user.getBirthdate());

        List<BankAccountDTO> accountDTOS = accounts.stream().map(
                bankAccount -> {
                    BankAccountDTO bankAccountDTO = new BankAccountDTO();
                    bankAccountDTO.setAmount(bankAccount.getBalance());
                    bankAccountDTO.setCurrency(bankAccount.getCurrency());
                    bankAccountDTO.setAccountType(bankAccount.getAccountType());
                    return bankAccountDTO;
                }
        ).toList();

        userProfileDTO.setAccounts(accountDTOS);

        return userProfileDTO;
    }
}
