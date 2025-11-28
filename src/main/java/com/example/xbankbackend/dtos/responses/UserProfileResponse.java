package com.example.xbankbackend.dtos.responses;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private Date birthdate;

    private List<BankAccountResponse> accounts;
}
