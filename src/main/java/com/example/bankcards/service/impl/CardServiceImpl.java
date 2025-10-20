package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionService encryptionService;
    private final CardMapper cardMapper;

    @Override
    public CardDto createCard(CreateCardRequest request) {
        User owner = getOwnerOrThrow(request.getOwnerId());

        String encryptedNumber = generateEncryptedCardNumber();

        Card card = buildNewCard(owner, encryptedNumber, request.getExpireDate());

        Card saved = cardRepository.save(card);
        log.info("Card with id={} created successfully", saved.getId());
        return cardMapper.toDto(saved);
    }

    @Override
    public CardDto blockCard(Long cardId) {
        Card card = getCardIdOrThrow(cardId);

        ensureCardIsNotAlreadyInStatus(card, CardStatus.BLOCKED);

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        log.info("Card with id={} blocked successfully", cardId);
        return cardMapper.toDto(savedCard);
    }

    @Override
    public CardDto activateCard(Long cardId) {
        Card card = getCardIdOrThrow(cardId);

        ensureCardIsNotAlreadyInStatus(card, CardStatus.ACTIVE);

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Card with id={} activated successfully", cardId);
        return cardMapper.toDto(savedCard);
    }

    @Override
    public void deleteCard(Long cardId) {
        Card card = getCardIdOrThrow(cardId);
        cardRepository.delete(card);
        log.info("Card with id={} deleted successfully", cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> listUserCards(UserDetails userDetails, Pageable pageable) {
        User owner = getOwnerByUsernameOrThrow(userDetails.getUsername());
        return cardRepository.findByOwner(owner, pageable).map(cardMapper::toDto);
    }

    @Override
    public CardDto requestBlockCard(UserDetails userDetails, Long cardId) {
        User owner = getOwnerByUsernameOrThrow(userDetails.getUsername());

        Card card = getCardByIdAndOwnerOrThrow(cardId, owner);

        card.setStatus(CardStatus.BLOCK_REQUEST);
        log.info("User={} requested block for card={}", owner.getUsername(), cardId);

        return cardMapper.toDto(card);
    }

    @Override
    public TransferDto transferBetweenCards(UserDetails userDetails, TransferRequest request) {
        User owner = getOwnerByUsernameOrThrow(userDetails.getUsername());

        Card from = getCardByIdAndOwnerOrThrow(request.getFromCardId(), owner);
        Card to = getCardByIdAndOwnerOrThrow(request.getToCardId(), owner);

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient funds to perform the operation");
            throw new InsufficientFundsException(from.getId());
        }
        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));
        log.info("User={} transferred={} from card={} to card={}",
                owner.getUsername(), request.getAmount(), from.getId(), to.getId());

        return TransferDto.builder()
                .fromCardId(from.getId())
                .toCardId(to.getId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .processedAt(Instant.now())
                .build();
    }

    private User getOwnerOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("OwnerId={} not found", id);
                    return new UserNotFoundException(id);
                });
    }

    private User getOwnerByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Username={} not found", username);
                    return new UserNotFoundException(username);
                });
    }

    private Card getCardIdOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CardId={} not found", id);
                    return new CardNotFoundException(id);
                });
    }

    private Card getCardByIdAndOwnerOrThrow(Long cardId, User owner) {
        return cardRepository.findByIdAndOwner(cardId, owner)
                .orElseThrow(() -> {
                    log.warn("CardId={} for UserId={} not found", cardId, owner.getId());
                    return new CardNotFoundException(cardId);
                });
    }

    private String generateEncryptedCardNumber() {
        String rawNumber = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        return encryptionService.encrypt(rawNumber);
    }

    private Card buildNewCard(User owner, String encryptedNumber, LocalDate expiryDate) {
        Card card = new Card();
        card.setEncryptedNumber(encryptedNumber);
        card.setOwner(owner);
        card.setExpiryDate(expiryDate);
        card.setStatus(CardStatus.NEW);
        card.setBalance(BigDecimal.ZERO);
        return card;
    }

    private void ensureCardIsNotAlreadyInStatus(Card card, CardStatus currentStatus) {
        if (card.getStatus() == currentStatus) {
            log.warn("Card={} is already={}", card.getId(), currentStatus);
            throw new CardOperationException("Card already " + currentStatus.toString());
        }
    }

}
