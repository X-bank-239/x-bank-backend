package com.example.xbankbackend.exceptions;

public class LoanRepaymentAmountMismatchException extends RuntimeException {
    public LoanRepaymentAmountMismatchException(String message) {
        super(message);
    }
}
