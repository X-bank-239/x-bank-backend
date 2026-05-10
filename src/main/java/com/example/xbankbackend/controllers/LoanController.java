package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanPaymentAmountResponse;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.services.loan.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
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

    @PostMapping("/{loanId}/repay/monthly")
    public ResponseEntity<LoanResponse> repayMonthly(@PathVariable UUID loanId,
                                                     @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing monthly repayment for loan {}", loanId);

        LoanResponse response = loanService.repayMonthly(loanId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/repay/early")
    public ResponseEntity<LoanResponse> repayEarly(@PathVariable UUID loanId,
                                                   @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing early repayment for loan {}", loanId);

        LoanResponse response = loanService.repayEarly(loanId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/payment-cost/early")
    public ResponseEntity<LoanPaymentAmountResponse> fullPaymentCost(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting full payment cost for loan {}", loanId);

        LoanPaymentAmountResponse response = loanService.fullPaymentCost(loanId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/payment-cost/monthly")
    public ResponseEntity<LoanPaymentAmountResponse> monthlyPaymentCost(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting monthly payment cost for loan {}", loanId);

        LoanPaymentAmountResponse response = loanService.monthlyPaymentCost(loanId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getByLoanId(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting loan by id {}", loanId);

        LoanResponse response = loanService.getByLoanId(loanId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/autopay/enable")
    public ResponseEntity<LoanResponse> enableAutopay(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Enabling autopay for loan {}", loanId);

        LoanResponse response = loanService.enableAutopay(loanId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/autopay/disable")
    public ResponseEntity<LoanResponse> disableAutopay(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Disabling autopay for loan {}", loanId);

        LoanResponse response = loanService.disableAutopay(loanId, userId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{loanId}/autopay/status")
    public ResponseEntity<LoanResponse> getAutopayStatus(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting autopay status for loan {}", loanId);

        LoanResponse response = loanService.getAutopayStatus(loanId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/repay/monthly")
    public ResponseEntity<LoanResponse> repayMonthlyByAccount(@PathVariable UUID accountId,
                                                              @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing monthly repayment for loan account {}", accountId);

        LoanResponse response = loanService.repayMonthlyByAccount(accountId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/autopay/enable")
    public ResponseEntity<LoanResponse> enableAutopayByAccount(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Enabling autopay for loan account {}", accountId);

        LoanResponse response = loanService.enableAutopayByAccount(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/autopay/disable")
    public ResponseEntity<LoanResponse> disableAutopayByAccount(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Disabling autopay for loan account {}", accountId);

        LoanResponse response = loanService.disableAutopayByAccount(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/autopay/status")
    public ResponseEntity<LoanResponse> getAutopayStatusByAccount(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting autopay status for loan account {}", accountId);

        LoanResponse response = loanService.getAutopayStatusByAccount(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/repay/early")
    public ResponseEntity<LoanResponse> repayEarlyByAccount(@PathVariable UUID accountId,
                                                            @Valid @RequestBody LoanRepaymentRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Processing early repayment for loan account {}", accountId);

        LoanResponse response = loanService.repayEarlyByAccount(accountId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/payment-cost/early")
    public ResponseEntity<LoanPaymentAmountResponse> fullPaymentCostByAccount(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting full payment cost for loan account {}", accountId);

        LoanPaymentAmountResponse response = loanService.fullPaymentCostByAccount(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/{accountId}/payment-cost/monthly")
    public ResponseEntity<LoanPaymentAmountResponse> monthlyPaymentCostByAccount(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting monthly payment cost for loan account {}", accountId);

        LoanPaymentAmountResponse response = loanService.monthlyPaymentCostByAccount(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<LoanResponse> getByAccountId(@PathVariable UUID accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting loans by account {}", accountId);

        LoanResponse response = loanService.getByAccountId(accountId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<LoanResponse>> getCurrentLoans() {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("Getting loans for user {}", userId);

        List<LoanResponse> loans = loanService.getLoansByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(loans);
    }
}
