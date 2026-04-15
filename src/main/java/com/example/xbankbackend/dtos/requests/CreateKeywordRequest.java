package com.example.xbankbackend.dtos.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateKeywordRequest {

    @NotNull(message = "keyword cannot be null")
    private String word;

    @NotNull(message = "categoryCode cannot be null")
    private String categoryCode;
}
