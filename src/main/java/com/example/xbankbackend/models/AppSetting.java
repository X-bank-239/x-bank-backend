package com.example.xbankbackend.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AppSetting {

    @NotNull
    private String settingKey;

    @NotNull
    private String settingValue;

    private String description;
    private String updatedAt;

    @NotNull
    private UUID updatedBy;
}
