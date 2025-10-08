package com.example.xbankbackend.models;

import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
public class Payment {
    private UUID paymentId;
    private UUID senderId;
    private UUID receiverId;
    private Float Amount;
    private String currency;
    private Timestamp Date;
}
