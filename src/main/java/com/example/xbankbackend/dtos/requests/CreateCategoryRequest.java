package com.example.xbankbackend.dtos.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateCategoryRequest {

    @NotNull(message = "category code cannot be null")
    private String code;

    @NotNull(message = "displayName cannot be null")
    private String displayName;

    @NotNull(message = "colorCode cannot be null")
    private String colorCode;
}
