package com.example.xbankbackend.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TempAuthState {
    private UUID id;
    private UUID userId;
    private String email;
    private String codeHash;
    private OffsetDateTime expiresAt;
    private boolean used;
}
