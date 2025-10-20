package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Card not found with id " + id);
    }

    public CardNotFoundException(String username) {
        super("Card not found with username " + username);
    }
}
