package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCardRequest {

    @NotNull
    private Long ownerId;

    @NotNull
    @Future(message = "Expire date must be in future")
    private LocalDate expireDate;
}
