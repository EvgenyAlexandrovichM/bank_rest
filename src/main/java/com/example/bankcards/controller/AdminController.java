package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.user.UpdateUserRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CardService cardService;

    @GetMapping("/users/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(defaultValue = "username") String sortBy) {
        return ResponseEntity.ok(userService.listUsers(page, size, sortBy));
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @PatchMapping("/cards/{id}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @PatchMapping("/cards/{id}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDto>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

}
