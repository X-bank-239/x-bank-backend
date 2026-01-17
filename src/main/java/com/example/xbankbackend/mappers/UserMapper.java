package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User requestToAccount(CreateUserRequest request);
}