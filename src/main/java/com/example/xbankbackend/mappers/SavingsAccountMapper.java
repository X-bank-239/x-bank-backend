package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateSavingsAccountRequest;
import com.example.xbankbackend.models.SavingsAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SavingsAccountMapper {
    SavingsAccount requestToAccount(CreateSavingsAccountRequest request);
}
