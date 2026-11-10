package com.banking.account_service.service;

import com.banking.account_service.dto.AccountRequest;
import com.banking.account_service.dto.AccountResponse;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public class AccountService {
    public @Nullable AccountResponse createAccount(AccountRequest accountRequest) {
        return null;
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
