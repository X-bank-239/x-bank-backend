package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.LoanPaymentAmountResponse;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.services.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/create")
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody CreateLoanRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Creating loan for user {}", userId);
        LoanResponse response = loanService.createLoan(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/credit-accounts/{creditAccountId}/repay/monthly")
    public ResponseEntity<LoanResponse> repayMonthly(@PathVariable UUID creditAccountId,
                                                     @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing monthly repayment for credit account {}", creditAccountId);
        LoanResponse response = loanService.repayMonthly(creditAccountId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/credit-accounts/{creditAccountId}/repay/early")
    public ResponseEntity<LoanResponse> repayEarly(@PathVariable UUID creditAccountId,
                                                   @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing early repayment for credit account {}", creditAccountId);
        LoanResponse response = loanService.repayEarly(creditAccountId, request, userId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/credit-accounts/{creditAccountId}/payment-cost/early")
    public ResponseEntity<LoanPaymentAmountResponse> fullPaymentCost(@PathVariable UUID creditAccountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting full payment cost for credit account {}", creditAccountId);
        LoanPaymentAmountResponse response = loanService.fullPaymentCost(creditAccountId, userId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/credit-accounts/{creditAccountId}/payment-cost/monthly")
    public ResponseEntity<LoanPaymentAmountResponse> monthlyPaymentCost(@PathVariable UUID creditAccountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting monthly payment cost for credit account {}", creditAccountId);
        LoanPaymentAmountResponse response = loanService.monthlyPaymentCost(creditAccountId, userId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/get-by-credit-account/{creditAccountId}")
    public ResponseEntity<LoanResponse> getByCreditAccount(@PathVariable UUID creditAccountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        LoanResponse response = loanService.getByCreditAccount(creditAccountId, userId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/list")
    public ResponseEntity<List<LoanResponse>> getCurrentLoans(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Getting accounts for user {}", userId);
        List<LoanResponse> loans = loanService.getLoansByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(loans);
    }

}
