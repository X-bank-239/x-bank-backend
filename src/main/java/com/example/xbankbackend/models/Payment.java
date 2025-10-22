package com.example.xbankbackend.models;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
public class Payment {
    private UUID paymentId;
    private UUID senderId;

    @NotNull(message = "receiverId cannot be null")
    private UUID receiverId;

    @NotNull(message = "amount cannot be null")
    private Float amount;

    @NotNull(message = "currency cannot be blank")
    private String currency;
    private Timestamp date;
}
