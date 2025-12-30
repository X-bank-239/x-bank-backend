package com.example.xbankbackend.exceptions;

public class UserIsNotABankAccountOwner extends RuntimeException {
    public UserIsNotABankAccountOwner(String message) {
        super(message);
    }
}
