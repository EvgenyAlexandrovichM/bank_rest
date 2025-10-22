package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.user.UpdateUserRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.security.DbUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CardService cardService;

    private UserDto userDto;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("user")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        cardDto = CardDto.builder()
                .id(10L)
                .cardNumber("1234567812345678")
                .ownerUsername("user")
                .expiryDate(LocalDate.of(2028, 10, 20))
                .status("ACTIVE")
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_returns200() throws Exception {
        when(userService.findByUsername("user")).thenReturn(userDto);

        performGet("/api/admin/users/username/user",
                status().isOk(),
                jsonPath("$.username").value("user"));
        verify(userService).findByUsername("user");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_returns404_whenNotFound() throws Exception {
        when(userService.findByUsername("user")).thenThrow(new UserNotFoundException("user"));

        performGet("/api/admin/users/username/user", status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_returns200() throws Exception {
        when(userService.findById(1L)).thenReturn(userDto);

        performGet("/api/admin/users/1",
                status().isOk(),
                jsonPath("$.username").value("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_returns404_whenNotFound() throws Exception {
        when(userService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        performGet("/api/admin/users/99", status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_returns200_whenValid() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("newuser", "P@ssw0rd!", Set.of("ROLE_ADMIN"));
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(UserDto.builder().id(1L).username("newuser").roles(Set.of("ROLE_ADMIN")).build());

        performPatch("/api/admin/users/1", request,
                status().isOk(),
                jsonPath("$.username").value("newuser"),
                jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_returns400_whenInvalidRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("baduser", "badpassword", null);

        performPatch("/api/admin/users/1", request, status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_returns409_whenUsernameExists() throws Exception {
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("user"));

        performPatch("/api/admin/users/1",
                new UpdateUserRequest("user", "P@ssw0rd!", Set.of()),
                status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCard_returns201() throws Exception {
        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(cardDto);

        performPost("/api/admin/cards",
                new CreateCardRequest(1L, LocalDate.of(2028, 10, 20)),
                status().isCreated(),
                jsonPath("$.id").value(10),
                jsonPath("$.ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void blockCard_returns200() throws Exception {
        when(cardService.blockCard(10L)).thenReturn(cardDto);

        performPatch("/api/admin/cards/10/block", null,
                status().isOk(),
                jsonPath("$.ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void activateCard_returns200() throws Exception {
        when(cardService.activateCard(10L)).thenReturn(cardDto);

        performPatch("/api/admin/cards/10/activate", null,
                status().isOk(),
                jsonPath("$.ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCard_returns204() throws Exception {
        performDelete("/api/admin/cards/10", status().isNoContent());
        verify(cardService).deleteCard(10L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllCards_returns200() throws Exception {
        Page<CardDto> page = new PageImpl<>(List.of(cardDto));
        when(cardService.getAllCards(any(Pageable.class))).thenReturn(page);

        performGet("/api/admin/cards",
                status().isOk(),
                jsonPath("$.content[0].ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getUserById_returns403_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createCard_returns403_forNonAdmin() throws Exception {
        CreateCardRequest request = new CreateCardRequest(1L, LocalDate.of(2028, 10, 20));

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void blockCard_returns403_forNonAdmin() throws Exception {
        mockMvc.perform(patch("/api/admin/cards/10/block").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteCard_returns403_forNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/10").with(csrf()))
                .andExpect(status().isForbidden());
    }

    private void performGet(String url, ResultMatcher... matchers) throws Exception {
        ResultActions actions = mockMvc.perform(get(url));
        for (ResultMatcher matcher : matchers) {
            actions.andExpect(matcher);
        }
    }

    private void performPost(String url, Object body, ResultMatcher... matchers) throws Exception {
        ResultActions actions = mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
        for (ResultMatcher matcher : matchers) {
            actions.andExpect(matcher);
        }
    }

    private void performPatch(String url, Object body, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder = patch(url)
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

    private void performDelete(String url, ResultMatcher... matchers) throws Exception {
        ResultActions actions = mockMvc.perform(delete(url)
                .with(csrf())
        );
        for (ResultMatcher matcher : matchers) {
            actions.andExpect(matcher);
        }
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
