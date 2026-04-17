package com.example.xbankbackend.services.bankAccount;

import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.repositories.BankAccountRepository;
import com.example.xbankbackend.services.user.UserValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class BankAccountService {

    private BankAccountRepository bankAccountRepository;
    private BankAccountMapper bankAccountMapper;

    private final BankAccountValidationService bankAccountValidationService;
    private final UserValidationService userValidationService;

    public void create(BankAccount bankAccount) {
        userValidationService.validateUserExists(bankAccount.getUserId());

        bankAccount.setBalance(BigDecimal.ZERO);
        bankAccount.setAccountId(UUID.randomUUID());
        bankAccount.setActive(true);

        bankAccountRepository.create(bankAccount);
    }

    public BankAccountResponse get(UUID accountId) {
        bankAccountValidationService.validateBankAccountExists(accountId);

        BankAccount bankAccount = bankAccountRepository.get(accountId);

        return bankAccountMapper.accountToResponse(bankAccount);
    }

    public List<BankAccountResponse> getAccountsByUser(UUID userId) {
        userValidationService.validateUserExists(userId);

        List<BankAccount> bankAccounts = bankAccountRepository.getBankAccounts(userId);

        return bankAccountMapper.accountsToResponses(bankAccounts);
    }

    public void deactivateAccount(UUID accountId) {
        bankAccountValidationService.validateBankAccountExists(accountId);

        bankAccountRepository.deactivate(accountId);
    }
}
