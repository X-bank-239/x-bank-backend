package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateTransactionRequest;
import com.example.xbankbackend.models.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction requestToTransaction(CreateTransactionRequest request);
}
