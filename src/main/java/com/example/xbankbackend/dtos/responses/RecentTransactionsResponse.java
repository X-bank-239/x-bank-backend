package com.example.xbankbackend.dtos.responses;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RecentTransactionsResponse {
    private Integer total;
    private Integer page;
    private Integer size;
    private List<TransactionResponse> transactions;
}
