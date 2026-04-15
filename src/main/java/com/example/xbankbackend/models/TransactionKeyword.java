package com.example.xbankbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionKeyword {
    private String word;
    private String categoryCode;
    private OffsetDateTime createdAt;
}
