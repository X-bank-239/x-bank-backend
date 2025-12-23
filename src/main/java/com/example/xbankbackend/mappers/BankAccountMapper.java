package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.models.BankAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    BankAccount requestToAccount(CreateBankAccountRequest request);
}
