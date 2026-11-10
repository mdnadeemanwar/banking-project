package com.banking.account_service.repository;

import com.banking.account_service.entity.Account;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);
}
