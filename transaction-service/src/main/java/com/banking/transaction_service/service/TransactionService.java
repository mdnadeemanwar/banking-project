package com.banking.transaction_service.service;

import com.banking.transaction_service.dto.TransactionResponse;
import com.banking.transaction_service.dto.TransferRequest;
import com.banking.transaction_service.repository.TransactionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // First create the kafka topic

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";


    /**
     * SAGA step no 1 :- initiate transfer
     * Deduct from the sender
     * saves transaction PROCESSING
     * publish event to kafka for fraud check
     * Return
     *
     * @param transferRequest
     * @return
     */
    public TransactionResponse transfer(TransferRequest transferRequest) {

        String senderAccountNo = transferRequest.getSenderAccountNumber();
        String receiverAccountNo = transferRequest.getReceiverAccountNumber();
        BigDecimal amount = transferRequest.getAmount();

        log.info("SAGA START - Transfer : {} -> {} amount: {}", senderAccountNo, receiverAccountNo, amount);

        // 1. validate: sender != receiver
        if (transferRequest.getSenderAccountNumber().equals(transferRequest.getReceiverAccountNumber())) {
            throw new RuntimeException("Sender and Receiver account can not be the same");
        }

        // 2. generate transactionId

        String transactionId = UUID.randomUUID().toString();

        // 3. deduct from sender (call account-service)


        // 4. save transaction with status PROCESSING

        // 5. publish event to Kafka for the fraud check

        // 6. return TransactionResponse

    }

    public @Nullable TransactionResponse getTransaction(String transactionId) {
        return null;
    }

    public @Nullable List<TransactionResponse> getAllTransaction(String accountnumber) {
        return null;
    }

    public @Nullable TransactionResponse verifyOTP(String transactionId, String otp) {
        return null;
    }
}
