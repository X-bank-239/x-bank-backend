package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.models.BankAccount;
import com.fasterxml.jackson.databind.util.Converter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    BankAccount requestToAccount(CreateBankAccountRequest request);
}
