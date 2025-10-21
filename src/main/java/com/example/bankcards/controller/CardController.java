package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardBalanceDto;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CardController {

    private final CardService cardService;

    @GetMapping("/user")
    public ResponseEntity<Page<CardDto>> listUserCards(@AuthenticationPrincipal UserDetails userDetails,
                                                       Pageable pageable) {
        return ResponseEntity.ok(cardService.listUserCards(userDetails, pageable));
    }

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<CardDto> requestBlockCard(@PathVariable Long cardId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.requestBlockCard(userDetails, cardId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferDto> transferBetweenCards(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody TransferRequest transferRequest) {
        return ResponseEntity.ok(cardService.transferBetweenCards(userDetails, transferRequest));
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<CardBalanceDto> getBalance(@PathVariable Long cardId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getBalance(userDetails, cardId));
    }
}
