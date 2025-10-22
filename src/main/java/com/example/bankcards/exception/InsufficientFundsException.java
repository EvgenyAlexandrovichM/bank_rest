package com.example.bankcards.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long id) {
        super("Insufficient funds to perform the operation" + id);
    }
}
