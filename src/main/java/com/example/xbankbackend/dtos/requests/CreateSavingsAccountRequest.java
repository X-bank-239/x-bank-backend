package com.example.xbankbackend.dtos.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateSavingsAccountRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    @Future
    private LocalDate maturityDate;

    @NotNull
    private boolean allowWithdrawal;

    @NotNull
    private boolean allowTopup;

    @DecimalMin("0")
    private BigDecimal earlyWithdrawalPenalty = BigDecimal.valueOf(100);
}
