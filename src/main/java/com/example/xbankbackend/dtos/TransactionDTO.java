package com.example.xbankbackend.dtos;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TransactionDTO {
    private TransactionType transactionType;
    private String senderName;
    private String receiverName;
    private Float amount;
    private CurrencyType currency;
    private OffsetDateTime transactionDate;
    private String comment;
}
