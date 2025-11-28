package com.example.xbankbackend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecentTransactionsResponse {
    private Integer total;
    private Integer page;
    private Integer size;
    private List<TransactionResponse> transactions;
}
