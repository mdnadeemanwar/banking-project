package com.banking.transaction_service.service;

import com.banking.transaction_service.client.AccountServiceClient;
import com.banking.transaction_service.dto.TransactionResponse;
import com.banking.transaction_service.dto.TransferRequest;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.entity.TransactionStatus;
import com.banking.transaction_service.entity.TransactionType;
import com.banking.transaction_service.repository.TransactionRepository;
import feign.FeignException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final AccountServiceClient accountServiceClient;

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
        if (senderAccountNo.equals(receiverAccountNo)) {
            throw new RuntimeException("Sender and Receiver account can not be the same");
        }


        // 2. generate transactionId
        String transactionId = UUID.randomUUID().toString();

        // 3. save transaction with status PROCESSING
        Transaction transaction = Transaction.builder()
                .senderAccountNumber(senderAccountNo)
                .receiverAccountNumber(receiverAccountNo)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PROCESSING)
                .description(transferRequest.getDescription())
                .referenceNumber(transactionId)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as PROCESSING : {}", savedTransaction.getId());

        // 4. deduct from sender (call account-service)
        try {
            accountServiceClient.deductBalance(senderAccountNo, amount);
        } catch (FeignException e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            log.error("Debit failed for transaction id :{}", transactionId, e);
            throw new RuntimeException("Transfer failed: could not debit sender account");
        }


        // 5. publish event to Kafka for the fraud check

        TransactionEventConsumer transactionEventConsumer = new TransactionEventConsumer(
                transactionId,
                savedTransaction.getSenderAccountNumber(),
                savedTransaction.getReceiverAccountNumber(),
                savedTransaction.getAmount()
        );

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, transactionEventConsumer);
        log.info("Published transaction.initiated, transactionId: {}", transactionId);


        // 6. return TransactionResponse
        return mapToResponse(savedTransaction);

    }

    private TransactionResponse mapToResponse(Transaction savedTransaction) {
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .referenceNumber(savedTransaction.getReferenceNumber())
                .senderAccountNumber(savedTransaction.getSenderAccountNumber())
                .receiverAccountNumber(savedTransaction.getReceiverAccountNumber())
                .amount(savedTransaction.getAmount())
                .type(savedTransaction.getType())
                .status(savedTransaction.getStatus())
                .description(savedTransaction.getDescription())
                .createdAt(savedTransaction.getCreatedAt())
                .build();

    }

    public TransactionResponse getTransaction(String transactionId) {
        return null;
    }

    public List<TransactionResponse> getAllTransaction(String accountnumber) {
        return null;
    }

    public TransactionResponse verifyOTP(String transactionId, String otp) {
        return null;
    }
}
