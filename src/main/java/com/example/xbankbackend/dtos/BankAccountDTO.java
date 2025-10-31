package com.example.xbankbackend.dtos;

import com.example.xbankbackend.enums.BankAccountType;
import com.example.xbankbackend.enums.CurrencyType;
import lombok.Data;

@Data
public class BankAccountDTO {
    private Float amount;
    private CurrencyType currency;
    private BankAccountType accountType;
}
