package com.banking.transaction_service.service;

import java.math.BigDecimal;

public record TransactionEventConsumer (
    String transactionId,
    String senderAccountNumber,
    String receiverAccountNumber,
    BigDecimal amount
    ){}
