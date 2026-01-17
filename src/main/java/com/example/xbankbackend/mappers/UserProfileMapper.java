package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserProfileMapper {
    public UserProfileResponse map(User user, List<BankAccount> account){
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .birthdate(user.getBirthdate())
                .accounts(account.stream().map(
                        bankAccount -> {
                            BankAccountResponse bankAccountResponse = new BankAccountResponse();
                            bankAccountResponse.setAccountId(bankAccount.getAccountId());
                            bankAccountResponse.setBalance(bankAccount.getBalance());
                            bankAccountResponse.setCurrency(bankAccount.getCurrency());
                            bankAccountResponse.setAccountType(bankAccount.getAccountType());
                            return bankAccountResponse;
                        }
                ).toList())
                .build();
        return userProfileResponse;
    }
}
