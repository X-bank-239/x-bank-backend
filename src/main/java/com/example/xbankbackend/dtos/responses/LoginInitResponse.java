package com.example.xbankbackend.dtos.responses;

import lombok.Data;

@Data
public class LoginInitResponse {
    private boolean requires2fa;
    private String tempToken;
    private String email;
}
