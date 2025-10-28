package com.example.xbankbackend.models.dto;

import com.example.xbankbackend.enums.BankAccountType;
import lombok.Data;

import java.util.UUID;

@Data
public class BankAccountRegistrationDTO {
    private UUID userId;
    private BankAccountType accountType;
}
