package com.example.bankcards.dto.card;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBalanceDto {

    private Long id;

    private String cardNumberMasked;

    private BigDecimal balance;
}
