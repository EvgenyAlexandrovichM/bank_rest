package com.example.bankcards.dto.card;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDto {

    private Long id;
    private String cardNumber;
    private String ownerUsername;
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;
}
