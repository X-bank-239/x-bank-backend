package com.example.xbankbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecentTransactionsDTO {
    private Integer total;
    private Integer page;
    private Integer size;
    private List<TransactionDTO> transactions;
}
