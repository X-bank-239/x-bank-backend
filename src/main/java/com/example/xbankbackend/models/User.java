package com.example.xbankbackend.models;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class User {
    private UUID userId;
    private String firstName;
    private String lastName;

    @Email
    private String email;
    private Date birthdate;
    private String password;
}
