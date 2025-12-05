package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateBankAccountRequest;
import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.models.BankAccount;
import com.example.xbankbackend.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User requestToAccount(CreateUserRequest request);
}