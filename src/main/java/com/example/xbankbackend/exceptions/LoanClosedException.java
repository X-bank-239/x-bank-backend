package com.example.xbankbackend.exceptions;

public class LoanClosedException extends RuntimeException {
    public LoanClosedException(String message) {
        super(message);
    }
}
