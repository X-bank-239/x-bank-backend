package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateLoanRequest;
import com.example.xbankbackend.dtos.requests.LoanRepaymentRequest;
import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.jwt.SecurityUtil;
import com.example.xbankbackend.services.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> get(@PathVariable UUID loanId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        LoanResponse response = loanService.get(loanId, userId);
        return ResponseEntity.ok(response);
    }

}
