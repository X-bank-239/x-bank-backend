package com.example.xbankbackend.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String email;
    private Date birthdate;

    private List<BankAccountDTO> accounts;
}
