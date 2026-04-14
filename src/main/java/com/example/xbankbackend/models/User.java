package com.example.xbankbackend.models;

import com.example.xbankbackend.enums.UserRole;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User {

    private UUID userId;

    @NotNull(message = "first name cannot be null")
    private String firstName;

    @NotNull(message = "last name cannot be null")
    private String lastName;

    @NotNull(message = "user role cannot be null")
    private UserRole role;

    @Email
    @NotNull(message = "email cannot be null")
    private String email;

    @NotNull(message = "birthdate cannot be null")
    private Date birthdate;

    @NotNull(message = "password cannot be null")
    private String password;

    @NotNull(message = "active/inactive cannot be null")
    private Boolean active;
}
