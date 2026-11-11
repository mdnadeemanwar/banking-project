package com.banking.account_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountService accountService;

    /**
     * Consume transaction.completed event from the kafka
     * credit receiver account
     *
     * @param payload
     */

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(@Payload Map<String, Object> payload) {

        try {
            String receiverAccount = (String) payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            log.info("Crediting account : {} amount :{} ", receiverAccount, amount);
            accountService.creditBalance(receiverAccount, amount);
        } catch (Exception e) {
            log.error("Error creaditing account :{}", e.getMessage());
        }
    }

    /**
     * if we detect the fraud then we need to block the account
     */

    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(@Payload Map<String, Object> payload) {

        try {
            String accountNumber = (String) payload.get("accountNumber");
            log.info("Fraud detected and blocking the account number :{}", accountNumber);
            accountService.blockAccountByAccountNumber(accountNumber);
        } catch (Exception e) {
            log.error("Error blocking account :{}", e.getMessage());
        }
    }

}
