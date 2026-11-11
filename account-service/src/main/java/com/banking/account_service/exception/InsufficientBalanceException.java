package com.banking.account_service.exception;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String accountNumber) {
        super("Insufficient balance");
    }
}
