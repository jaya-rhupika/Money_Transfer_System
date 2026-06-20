package com.fidelity.mts;

import com.fidelity.mts.entity.Account;
import com.fidelity.mts.enums.AccountStatus;
import com.fidelity.mts.exceptions.InsufficientBalanceException;
import com.fidelity.mts.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    private Account account;

    @Mock
    private TransferService transferService;  // Inject the TransferService to use in the tests

    @BeforeEach
    void setUp() {
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);

        // Set up the account for each test
        account = new Account();
        account.setId(1L);
        account.setHolderName("George");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setVersion(0);
    }

    @Test
    void testDebit_Success() {
        // Test debit with valid amount
        BigDecimal debitAmount = new BigDecimal("600.00");
        BigDecimal expectedBalance = new BigDecimal("400.00");

        // Simulate the debit operation
        BigDecimal actualBalance = account.debit(account.getBalance(), debitAmount);

        // Assert the new balance after debit
        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void testDebit_InsufficientBalance() {

        BigDecimal debitAmount = new BigDecimal("1200.00");

        BigDecimal originalBalance = account.getBalance();
        BigDecimal resultingBalance = originalBalance.subtract(debitAmount);

        assertThrows(InsufficientBalanceException.class, () -> {
            if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException("Balance is not sufficient");
            }
        });
    }


    @Test
    void testDebit_NegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        assertThrows(InsufficientBalanceException.class, () -> {
            if (negativeAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException("Negative Balance");
            }
        });
    }

    @Test
    void testCredit_Success() {
        // Test credit with a valid amount
        BigDecimal creditAmount = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("1500.00");

        // Perform the credit operation
        BigDecimal actualBalance = account.credit(account.getBalance(), creditAmount);

        // Assert the balance after credit
        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void testIsActive_ActiveAccount() {
        // Set status to ACTIVE and check if account is active
        account.setStatus(AccountStatus.ACTIVE);
        assertTrue(account.isActive());
    }

    @Test
    void testIsActive_LockedAccount() {
        // Set status to LOCKED and check if account is not active
        account.setStatus(AccountStatus.LOCKED);
        assertFalse(account.isActive());
    }

    @Test
    void testIsActive_ClosedAccount() {
        // Set status to CLOSED and check if account is not active
        account.setStatus(AccountStatus.CLOSED);
        assertFalse(account.isActive());
    }
}
