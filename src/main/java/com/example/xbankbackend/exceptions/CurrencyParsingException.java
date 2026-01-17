package com.example.xbankbackend.exceptions;

public class CurrencyParsingException extends RuntimeException {
    public CurrencyParsingException(String message) {
        super(message);
    }
}
