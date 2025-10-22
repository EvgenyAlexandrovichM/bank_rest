package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface CardService {
    CardDto createCard(CreateCardRequest request);

    CardDto blockCard(Long cardId);

    CardDto activateCard(Long cardId);

    void deleteCard(Long cardId);

    Page<CardDto> getAllCards(Pageable pageable);

    Page<CardDto> listUserCards(UserDetails userDetails, Pageable pageable);

    CardBalanceDto getBalance(UserDetails userDetails, Long cardId);

    CardDto requestBlockCard(UserDetails userDetails, Long cardId);

    TransferDto transferBetweenCards(UserDetails userDetails, TransferRequest request);

}
