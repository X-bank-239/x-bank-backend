package com.example.xbankbackend.dtos.responses;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import lombok.Data;

@Data
public class BankAccountResponse {
    private Float amount;
    private CurrencyType currency;
    private BankAccountType accountType;
}
