package com.example.bankcards.dto.card;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDto {

    private Long fromCardId;

    private Long toCardId;

    private BigDecimal amount;

    private String description;

    private Instant processedAt;
}
