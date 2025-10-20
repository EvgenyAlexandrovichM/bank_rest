package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    private CardDto cardDto;
    private TransferDto transferDto;

    @BeforeEach
    void setUp() {
        cardDto = CardDto.builder()
                .id(10L)
                .cardNumber("1234567812345678")
                .ownerUsername("user")
                .expiryDate(LocalDate.of(2028, 10, 20))
                .status("ACTIVE")
                .balance(BigDecimal.valueOf(1000))
                .build();

        transferDto = TransferDto.builder()
                .fromCardId(10L)
                .toCardId(20L)
                .amount(BigDecimal.valueOf(200))
                .description("Test transfer")
                .processedAt(Instant.now())
                .build();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void listUserCards_returns200() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(cardDto));
        when(cardService.listUserCards(any(UserDetails.class), any(Pageable.class))).thenReturn(page);

        performGet("/api/cards/user/1",
                status().isOk(),
                jsonPath("$.content[0].id").value(10),
                jsonPath("$.content[0].ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void requestBlockCard_returns200() throws Exception {
        when(cardService.requestBlockCard(any(UserDetails.class), eq(10L))).thenReturn(cardDto);

        performPost("/api/cards/request-block/10", null,
                status().isOk(),
                jsonPath("$.id").value(10),
                jsonPath("$.ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void requestBlockCard_returns404_whenNotFound() throws Exception {
        when(cardService.requestBlockCard(any(UserDetails.class), eq(99L)))
                .thenThrow(new CardNotFoundException(99L));

        performPost("/api/cards/request-block/99", null, status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void transferBetweenCards_returns200() throws Exception {
        TransferRequest request = new TransferRequest(10L,
                20L, BigDecimal.valueOf(200),
                "Test transfer",
                Instant.now());
        when(cardService.transferBetweenCards(any(UserDetails.class), any(TransferRequest.class)))
                .thenReturn(transferDto);

        performPost("/api/cards/transfer", request,
                status().isOk(),
                jsonPath("$.fromCardId").value(10),
                jsonPath("$.toCardId").value(20),
                jsonPath("$.amount").value(200),
                jsonPath("$.description").value("Test transfer"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void transferBetweenCards_returns400_whenInvalidRequest() throws Exception {
        TransferRequest invalid = new TransferRequest(null,
                null,
                BigDecimal.valueOf(-10),
                "",
                Instant.now());

        performPost("/api/cards/transfer", invalid, status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void transferBetweenCards_returns404_whenCardNotFound() throws Exception {
        TransferRequest request = new TransferRequest(10L,
                99L,
                BigDecimal.valueOf(200),
                "fail",
                Instant.now());
        when(cardService.transferBetweenCards(any(UserDetails.class), any(TransferRequest.class)))
                .thenThrow(new CardNotFoundException(99L));

        performPost("/api/cards/transfer", request, status().isNotFound());
    }

    private void performGet(String url, ResultMatcher... matchers) throws Exception {
        ResultActions actions = mockMvc.perform(get(url));
        for (ResultMatcher matcher : matchers) {
            actions.andExpect(matcher);
        }
    }

    private void performPost(String url, Object body, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder = post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            builder.content(toJson(body));
        }
        ResultActions actions = mockMvc.perform(builder);
        for (ResultMatcher matcher : matchers) {
            actions.andExpect(matcher);
        }
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
