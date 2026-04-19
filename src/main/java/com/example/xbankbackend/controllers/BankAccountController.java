package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.mappers.BankAccountMapper;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.services.bankAccount.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/bank-account")
public class BankAccountController {

    private BankAccountMapper bankAccountMapper;
    private BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<BankAccount> create(@Valid @RequestBody CreateBankAccountRequest bankAccountRequest) {
        UUID userId = SecurityUtil.getCurrentUserId();

        log.info("Creating bank account: {}", bankAccountRequest);
        BankAccount bankAccount = bankAccountMapper.requestToAccount(bankAccountRequest);
        bankAccount.setUserId(userId);
        bankAccountService.create(bankAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

    @GetMapping("/list")
    public ResponseEntity<List<BankAccountResponse>> getCurrentUserAccounts(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Getting accounts for user {}", userId);
        List<BankAccountResponse> bankAccount = bankAccountService.getAccountsByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
    }

    // ADMIN-only

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountResponse> getAccountById(@PathVariable UUID accountId) {
        log.info("Getting account with id {}", accountId);
        BankAccountResponse bankAccount = bankAccountService.get(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
    }

    @GetMapping("/list/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountResponse> getAccountsByUserId(@PathVariable UUID userId) {
        log.info("Getting account with for user {}", userId);
        BankAccountResponse bankAccount = bankAccountService.get(userId);
        return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
    }

    // ADMIN-only

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable UUID accountId) {
        log.info("Deactivating account {}", accountId);
        bankAccountService.deactivateAccount(accountId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{accountId}/unblock")
    public ResponseEntity<Void> reactivateAccount(@PathVariable UUID accountId) {
        log.info("Reactivating account {}", accountId);
        bankAccountService.reactivateAccount(accountId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
