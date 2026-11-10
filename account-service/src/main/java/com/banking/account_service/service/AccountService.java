package com.banking.account_service.service;

import com.banking.account_service.dto.AccountRequest;
import com.banking.account_service.dto.AccountResponse;
import com.banking.account_service.entity.Account;
import com.banking.account_service.entity.AccountStatus;
import com.banking.account_service.entity.AccountType;
import com.banking.account_service.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@Data
@AllArgsConstructor
@RequiredArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountResponse createAccount(AccountRequest accountRequest) {
        log.info("Creating account for : {}", accountRequest.getEmail());
        if (accountRepository.existByEmail(accountRequest.getEmail())) {
            throw new RuntimeException("Account Already Exists for Emails: " + accountRequest.getEmail());
        }


        // we are using builder to create the object
        Account account = Account.builder()
                .accountHolderName(accountRequest.getAccountHolderName())
                .email(accountRequest.getEmail())
                .phone(accountRequest.getPhone())
                .accountType(accountRequest.getAccountType())
                .accountType(accountRequest.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .balance(accountRequest.getInitialDepoist())
                .accountNumber(generateAccountNumber())
                .dailyTransactionLimit(
                        accountRequest.getAccountType() == AccountType.SAVING
                                ? new BigDecimal("100000")
                                : new BigDecimal("500000"))
                .build();

        Account savedAccount = accountRepository.save(account);

        log.info("Account Created : {}", savedAccount.getAccountNumber());

        /**
         * Before return we have to conver this saved account to Account response that's why
         * we are creating the mapToResponse Method
         */
        return mapToResponse(savedAccount);

    }

    private String generateAccountNumber() {
        String accountNumber;
        do{
            long number = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
            accountNumber = String.format("012d", number % 1_000_000_000_000L);
        }while(accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;

    }

    private AccountResponse mapToResponse(Account savedAccount) {
        return AccountResponse.builder()
                .id(savedAccount.getId())
                .accountNumber(savedAccount.getAccountNumber())
                .accountHolderName(savedAccount.getAccountHolderName())
                .email(savedAccount.getEmail())
                .phone(savedAccount.getPhone())
                .accountType(savedAccount.getAccountType())
                .accountStatus(savedAccount.getAccountStatus())
                .balance(savedAccount.getBalance())
                .dailyTransactionLimit(savedAccount.getDailyTransactionLimit())
                .createdAt(savedAccount.getCreatedAt())
                .build();

    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        return null;
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        return null;
    }

    public String blockAccountByAccountNumber(String accountNumber) {
        return null;
    }

    public void deductBalance(String accountNumber, BigDecimal amount) {

    }

    public void creditBalance(String accountNumber, BigDecimal amount) {

    }
}
