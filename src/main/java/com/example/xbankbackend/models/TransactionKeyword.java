package com.example.xbankbackend.models;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TransactionKeyword {
    private String word;
    private String categoryCode;
    private OffsetDateTime createdAt;
}
