package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.BankAccountType;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Getter
public class BankAccount {
    private UUID accountId;
    private UUID userId;
    private BigDecimal amount;
    private BankAccountType accountType;
}
