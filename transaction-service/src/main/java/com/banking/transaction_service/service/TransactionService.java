package com.banking.transaction_service.service;

import com.banking.transaction_service.dto.TransactionResponse;
import com.banking.transaction_service.dto.TransferRequest;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

public class TransactionService {
    public @Nullable TransactionResponse transfer(@Valid TransferRequest transferRequest) {
        return null;
    }

    public @Nullable TransactionResponse getTransaction(String transactionId) {
        return null;
    }

    public @Nullable TransactionResponse getAllTransaction(String accountnumber) {
        return null;
    }
}
