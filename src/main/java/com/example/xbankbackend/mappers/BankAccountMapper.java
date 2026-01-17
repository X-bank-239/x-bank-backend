package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.models.BankAccount;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    BankAccount requestToAccount(CreateBankAccountRequest request);
    BankAccountResponse accountToResponse(BankAccount bankAccount);
    List<BankAccountResponse> accountsToResponses(List<BankAccount> accounts);
}
