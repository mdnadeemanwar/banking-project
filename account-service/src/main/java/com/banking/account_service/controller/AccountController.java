package com.banking.account_service.controller;

import com.banking.account_service.dto.AccountRequest;
import com.banking.account_service.dto.AccountResponse;
import com.banking.account_service.entity.Account;
import com.banking.account_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    /**
     * Create a object of the service
     */

    private AccountService accountService;

    /**
     * Create new Account
     *
     * @param accountRequest
     * @return
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest accountRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(accountRequest));
    }

    /**
     * Get Account details about Account Number
     *
     * @param accountNumber
     * @return
     */

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }


    /**
     * Get Account Balance by Account Number
     *
     * @param accountNumber
     * @return
     */
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountBalance(accountNumber));
    }

    /**
     * Block account by Account number
     *
     * @param accountNumber
     * @return
     */
    @PatchMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.blockAccountByAccountNumber(accountNumber));
    }

    /**
     * SAGA Step 1 - deduct balance
     * called by trans service when trans is initiated
     */

    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    ) {
        accountService.deductBalance(accountNumber, amount);
        return ResponseEntity.ok("Amount deducated Successfully");
    }

    /**
     * SAGA Step 4 - compensating transaction endpoint
     * Called by transaction service in 2 scenerios
     * 1. Fraud Detected -> refund sender (undo step 1)
     * 2. Trans completed -> complete receiver
     */

    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    ) {
        accountService.creditBalance(accountNumber, amount);
        return ResponseEntity.ok("Balance Credited Successfully!");
    }


}
