package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private Card card;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .username("owner")
                .password("password")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(owner);

        card = Card.builder()
                .encryptedNumber("encrypted-1234567812345678")
                .owner(owner)
                .expiryDate(LocalDate.of(2028, 10, 20))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        cardRepository.save(card);
    }

    @Test
    void findByOwner_returnsPageWithCards_whenExists() {
        Page<Card> page = cardRepository.findByOwner(owner, PageRequest.of(0, 10));

        assertThat(page).isNotEmpty();
        assertThat(page.getContent().get(0).getOwner().getUsername()).isEqualTo("owner");
    }

    @Test
    void findByOwner_returnsEmptyPage_whenNoCards() {
        User another = User.builder()
                .username("another")
                .password("password")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(another);

        Page<Card> page = cardRepository.findByOwner(another, PageRequest.of(0, 10));

        assertThat(page).isEmpty();
    }

    @Test
    void findByIdAndOwner_returnsCard_whenExists() {
        Optional<Card> found = cardRepository.findByIdAndOwner(card.getId(), owner);

        assertThat(found).isPresent();
        assertThat(found.get().getEncryptedNumber()).isEqualTo("encrypted-1234567812345678");
    }

    @Test
    void findByIdAndOwner_returnsEmpty_whenNotExists() {
        User another = User.builder()
                .username("another")
                .password("password")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(another);

        Optional<Card> found = cardRepository.findByIdAndOwner(card.getId(), another);

        assertThat(found).isEmpty();
    }
}

