package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardEncryptionService encryptionService;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private User owner;
    private Card card;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .username("owner")
                .password("pass")
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        card = Card.builder()
                .id(10L)
                .encryptedNumber("encrypted-123")
                .owner(owner)
                .expiryDate(LocalDate.of(2028, 10, 20))
                .status(CardStatus.NEW)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        cardDto = CardDto.builder()
                .id(10L)
                .cardNumber("****5678")
                .ownerUsername("owner")
                .expiryDate(LocalDate.of(2028, 10, 20))
                .status("NEW")
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createCard_returnsDto_whenValid() {
        CreateCardRequest request = new CreateCardRequest(1L, LocalDate.of(2028, 10, 20));

        mockFindOwnerById();
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-123");
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.createCard(request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void blockCard_changesStatusToBlocked() {
        card.setStatus(CardStatus.ACTIVE);

        mockFindCardById(10L, card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.blockCard(10L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_throwsException_whenAlreadyBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        mockFindCardById(10L, card);

        assertThatThrownBy(() -> cardService.blockCard(10L))
                .isInstanceOf(CardOperationException.class);
    }

    @Test
    void activateCard_changesStatusToActive() {
        card.setStatus(CardStatus.NEW);

        mockFindCardById(10L, card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.activateCard(10L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_removesCard_whenExists() {
        mockFindCardById(10L, card);

        cardService.deleteCard(10L);

        verify(cardRepository).delete(card);
    }

    @Test
    void listUserCards_returnsPageOfDtos() {
        UserDetails userDetails = mockUserDetails();
        mockFindOwner();

        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByOwner(eq(owner), any(Pageable.class))).thenReturn(page);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        Page<CardDto> result = cardService.listUserCards(userDetails, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getOwnerUsername()).isEqualTo("owner");
    }

    @Test
    void requestBlockCard_setsStatusToBlockRequest() {
        UserDetails userDetails = mockUserDetails();
        mockFindOwner();
        mockFindCardByIdAndOwner(10L, card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        cardService.requestBlockCard(userDetails, 10L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCK_REQUEST);
    }

    @Test
    void transferBetweenCards_movesMoney_whenEnoughBalance() {
        UserDetails userDetails = mockUserDetails();
        mockFindOwner();

        Card from = Card.builder().id(1L).owner(owner).balance(BigDecimal.valueOf(100)).status(CardStatus.ACTIVE).build();
        Card to = Card.builder().id(2L).owner(owner).balance(BigDecimal.valueOf(50)).status(CardStatus.ACTIVE).build();
        mockFindCardByIdAndOwner(1L, from);
        mockFindCardByIdAndOwner(2L, to);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(30), "test", Instant.now());

        TransferDto result = cardService.transferBetweenCards(userDetails, request);

        assertThat(from.getBalance()).isEqualTo(BigDecimal.valueOf(70));
        assertThat(to.getBalance()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(30));
    }

    @Test
    void transferBetweenCards_throwsException_whenInsufficientFunds() {
        UserDetails userDetails = mockUserDetails();
        mockFindOwner();

        Card from = Card.builder().id(1L).owner(owner).balance(BigDecimal.valueOf(10)).status(CardStatus.ACTIVE).build();
        Card to = Card.builder().id(2L).owner(owner).balance(BigDecimal.valueOf(50)).status(CardStatus.ACTIVE).build();
        mockFindCardByIdAndOwner(1L, from);
        mockFindCardByIdAndOwner(2L, to);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(30), "test", Instant.now());

        assertThatThrownBy(() -> cardService.transferBetweenCards(userDetails, request))
                .isInstanceOf(InsufficientFundsException.class);
    }

    private UserDetails mockUserDetails() {
        return new org.springframework.security.core.userdetails.User(
                owner.getUsername(), owner.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private void mockFindOwner() {
        when(userRepository.findByUsername(owner.getUsername())).thenReturn(Optional.of(owner));
    }

    private void mockFindOwnerById() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
    }

    private void mockFindCardById(Long id, Card c) {
        when(cardRepository.findById(id)).thenReturn(Optional.of(c));
    }

    private void mockFindCardByIdAndOwner(Long id, Card c) {
        when(cardRepository.findByIdAndOwner(id, owner)).thenReturn(Optional.of(c));
    }
}
