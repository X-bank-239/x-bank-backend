package com.example.xbankbackend.dtos.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthUserRequest {
        @Email
        @NotNull(message = "email cannot be null")
        private String email;

        @NotNull(message = "password cannot be null")
        private String password;
}
