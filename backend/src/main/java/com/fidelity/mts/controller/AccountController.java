package com.fidelity.mts.controller;

import com.fidelity.mts.entity.Account;
import com.fidelity.mts.entity.TransactionLog;
import com.fidelity.mts.entity.UserCredentials;
import com.fidelity.mts.enums.AccountStatus;
import com.fidelity.mts.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fidelity.mts.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/")
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    @Autowired
    AccountService service;

    @Autowired
    UserCredentialsRepository userCredentialsRepository;

    @PostMapping
    public ResponseEntity<String> addAccount(@RequestBody Account e) {
        long id = service.addAccount(e);
        return ResponseEntity.status(HttpStatus.OK).body("Account with id " + id + " has been created");
    }

    /**
     * POST /api/v1/accounts/register
     * Body: { "holderName": "Ravi Kumar", "password": "secret" }
     *
     * Creates an account (auto-incremented ID), credits ₹200 welcome bonus,
     * and stores credentials using the auto-generated account ID as the username.
     * The user will log in with their account number + password.
     */
    @PostMapping("/accounts/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String holderName = body.get("holderName");
        String password   = body.get("password");

        if (holderName == null || holderName.isBlank() || password == null || password.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "holderName and password are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        // Create account — ID is auto-incremented by the DB
        Account account = new Account();
        account.setHolderName(holderName.trim());
        account.setBalance(new BigDecimal("200.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setLastUpdated(LocalDateTime.now());
        account.setTotalRewardPoints(0);

        long accountId = service.addAccount(account);

        // Use the auto-generated account ID as the login username
        String username = String.valueOf(accountId);
        UserCredentials creds = new UserCredentials(accountId, username, password);
        userCredentialsRepository.save(creds);

        Map<String, Object> res = new HashMap<>();
        res.put("accountId", accountId);
        res.put("message", "Account created successfully with ₹200 welcome bonus. Your account number is " + accountId + " — use it to log in.");
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccountsById(@PathVariable long id) {
        Account e = service.getAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body(e);
    }

    @GetMapping("/accounts/{id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalanceById(@PathVariable long id) {
        BigDecimal e = service.getBalance(id);
        return ResponseEntity.status(HttpStatus.OK).body(e);
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<TransactionLog>> getTransactionLogById(@PathVariable long id) {
        List<TransactionLog> e = service.getTransactions(id);
        return ResponseEntity.status(HttpStatus.OK).body(e);
    }
}
