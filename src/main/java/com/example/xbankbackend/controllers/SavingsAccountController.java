package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CloseSavingsRequest;
import com.example.xbankbackend.dtos.requests.CreateSavingsAccountRequest;
import com.example.xbankbackend.dtos.requests.ProlongSavingsRequest;
import com.example.xbankbackend.mappers.SavingsAccountMapper;
import com.example.xbankbackend.models.SavingsAccount;
import com.example.xbankbackend.services.savings.SavingsAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/savings")
public class SavingsAccountController {

    private SavingsAccountService savingsAccountService;
    private SavingsAccountMapper savingsAccountMapper;

    @PostMapping("/create")
    public ResponseEntity<SavingsAccount> create(@RequestBody @Valid CreateSavingsAccountRequest request, Authentication auth) {
        log.info("Creating savings account for user {}", auth.getName());

        SavingsAccount account = savingsAccountMapper.requestToAccount(request);
        savingsAccountService.create(account);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/interest")
    public ResponseEntity<BigDecimal> getInterest(@RequestParam boolean allowWithdrawal, @RequestParam boolean allowTopUp) {
        log.info("Getting interest, allowWithdrawal {}, allowTopUp {}", allowWithdrawal, allowTopUp);

        BigDecimal interest = savingsAccountService.getInterest(allowWithdrawal, allowTopUp);

        return ResponseEntity.status(HttpStatus.OK).body(interest);
    }

    @GetMapping("/list")
    public ResponseEntity<List<SavingsAccount>> getCurrentUserSavingsAccounts(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Getting savings accounts for user {}", userId);

        List<SavingsAccount> accounts = savingsAccountService.getByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    // CURRENT USER or ADMIN

    @GetMapping("/get/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isAccountOwner(#accountId, authentication)")
    public ResponseEntity<SavingsAccount> get(@PathVariable UUID accountId) {
        log.info("Getting account with id {}", accountId);

        SavingsAccount account = savingsAccountService.get(accountId);

        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PostMapping("/prolong/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isAccountOwner(#accountId, authentication)")
    public ResponseEntity<SavingsAccount> prolong(@PathVariable UUID accountId, @RequestBody ProlongSavingsRequest request) {
        log.info("Prolonging account with id {}", accountId);

        SavingsAccount account = savingsAccountService.prolong(accountId, request.getNewMaturityDate());

        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @DeleteMapping("/close/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.isAccountOwner(#accountId, authentication)")
    public ResponseEntity<Void> close(@PathVariable UUID accountId, @RequestBody CloseSavingsRequest request, Authentication auth) {
        log.info("Closing account with id {}", accountId);

        savingsAccountService.closeAccount(accountId, request.getTargetAccountId(), UUID.fromString(auth.getName()));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ADMIN-only

    @GetMapping("/list/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SavingsAccount>> getAllSavingsAccounts() {
        log.info("[ADMIN] Getting all active savings accounts");

        List<SavingsAccount> accounts = savingsAccountService.getAllActive();

        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }
}