package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateTransactionRequest;
import com.example.xbankbackend.dtos.responses.TransactionResponse;
import com.example.xbankbackend.models.Transaction;
import com.example.xbankbackend.services.user.UserService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    @Autowired
    protected UserService userService;

    public abstract Transaction requestToTransaction(CreateTransactionRequest request);

    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    public abstract TransactionResponse transactionToResponse(Transaction transaction);

    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    public abstract List<TransactionResponse> transactionsToResponses(List<Transaction> transactions);

    // TODO: оптимизировать
    @AfterMapping
    protected void fillNames(Transaction source, @MappingTarget TransactionResponse target) {
        if (source.getSenderId() != null) {
            target.setSenderName(userService.getNameByAccountId(source.getSenderId()));
        }
        if (source.getReceiverId() != null) {
            target.setReceiverName(userService.getNameByAccountId(source.getReceiverId()));
        }
    }
}
