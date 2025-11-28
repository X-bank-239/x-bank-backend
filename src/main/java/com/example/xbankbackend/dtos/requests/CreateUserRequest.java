package com.example.xbankbackend.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class CreateUserRequest {

    @NotNull(message = "first name cannot be null")
    private String firstName;

    @NotNull(message = "last name cannot be null")
    private String lastName;

    @Email
    @NotNull(message = "email cannot be null")
    private String email;

    @NotNull(message = "birthdate cannot be null")
    private Date birthdate;
}
