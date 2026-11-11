package com.banking.account_service.service;

import com.banking.account_service.controller.AccountController;
import com.banking.account_service.dto.AccountRequest;
import com.banking.account_service.dto.AccountResponse;
import com.banking.account_service.entity.Account;
import com.banking.account_service.entity.AccountStatus;
import com.banking.account_service.entity.AccountType;
import com.banking.account_service.exception.AccountNotActiveException;
import com.banking.account_service.exception.AccountNotFoundException;
import com.banking.account_service.exception.InsufficientBalanceException;
import com.banking.account_service.exception.InvalidAmountException;
import com.banking.account_service.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Data
@AllArgsConstructor
@RequiredArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final SecureRandom secureRandom = new SecureRandom();

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
        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
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
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(" Account Not Found"));
        return mapToResponse(account);
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        return account.getBalance();
    }

    public String blockAccountByAccountNumber(String accountNumber) {

        log.info("Blocking the Account :{}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));

        // 1. check if already blocked
        if (account.getAccountStatus() == AccountStatus.BLOCKED) {
            return "Account is already blocked";
        }
        // 2. set the status to BLOCKED
        account.setAccountStatus(AccountStatus.BLOCKED);
        // 3. save
        accountRepository.save(account);
        // 4. return your message
        return "Account has been Blocked !!";
    }

    /**
     * Deduct Balance from sender account and it is called via trans service
     *
     * @param accountNumber
     * @param amount
     */

    @Transactional
    public void deductBalance(String accountNumber, BigDecimal amount) {
        validateAmount(amount);
        log.info("Deducting balance : {} from account :{} ", amount, accountNumber);
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        //1. check the account status
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(accountNumber);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(accountNumber);
        }
        //2. Change tha Account Balance
        account.setBalance(account.getBalance().subtract(amount));
        //3. save
        accountRepository.save(account);
        log.info("Balance updated and new balance is :{}", account.getBalance());
    }


    @Transactional
    public void creditBalance(String accountNumber, BigDecimal amount) {
        validateAmount(amount);
        log.info("Crediting the balance :{}", accountNumber);
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        //1. check the account status
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(accountNumber);
        }
        //2. Change tha Account Balance
        account.setBalance(account.getBalance().add(amount));
        //3.save
        accountRepository.save(account);
        log.info("Amount : {} credited successfully to account :{}", amount, accountNumber);
    }

    private void validateAmount(BigDecimal amount) {
        if(amount == null || amount.compareTo(BigDecimal.ZERO)<=0){
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }
}
