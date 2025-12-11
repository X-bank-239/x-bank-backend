package com.example.xbankbackend.exceptions;

public class UserGivesIncorrectEmail extends RuntimeException {
    public UserGivesIncorrectEmail(String message) {
        super(message);
    }
}

