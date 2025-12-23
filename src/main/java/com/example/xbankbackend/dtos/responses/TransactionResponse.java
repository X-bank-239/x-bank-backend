package com.example.xbankbackend.dtos.responses;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class TransactionResponse {
    private TransactionType transactionType;
    private String senderName;
    private String receiverName;
    private BigDecimal amount;
    private CurrencyType currency;
    private OffsetDateTime transactionDate;
    private String comment;
}
