package com.example.xbankbackend.models;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
public class User {
    private UUID userId;
    private String firstName;
    private String lastName;

    @Email
    private String email;
    private Timestamp birthdate;
    private List<UUID> paymentsHistory;
}
