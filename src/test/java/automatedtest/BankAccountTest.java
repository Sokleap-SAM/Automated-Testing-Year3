package automatedtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the BankAccount class.
 */
public class BankAccountTest {
    private static final double INIT_BALANCE_100 = 100.0;
    private static final double INIT_BALANCE_50 = 50.0;
    private static final double INIT_BALANCE_30 = 30.0;
    private static final double DEPOSIT_25 = 25.0;
    private static final double WITHDRAW_40 = 40.0;
    private static final double WITHDRAW_5 = 5.0;
    private static final double NEGATIVE_10 = -10.0;
    private static final double NEGATIVE_5 = -5.0;
    private static final double NEGATIVE_100 = -100.0;
    private static final double EXPECTED_75 = 75.0;
    private static final double EXPECTED_60 = 60.0;

    /**
     * Tests that the initial balance is set correctly.
     */
    @Test
    void testInitialBalance() {
        BankAccount acc = new BankAccount(INIT_BALANCE_100);
        Assertions.assertEquals(INIT_BALANCE_100, acc.getBalance());
    }

    /**
     * Tests a valid deposit operation.
     */
    @Test
    void testDepositValid() {
        BankAccount acc = new BankAccount(INIT_BALANCE_50);
        acc.deposit(DEPOSIT_25);
        Assertions.assertEquals(EXPECTED_75, acc.getBalance());
    }

    /**
     * Tests deposit with an invalid (negative) amount.
     */
    @Test
    void testDepositInvalid() {
        BankAccount acc = new BankAccount(INIT_BALANCE_50);
        Assertions.assertThrows(IllegalArgumentException.class, () -> acc.deposit(NEGATIVE_10));
    }

    /**
     * Tests a valid withdrawal operation.
     */
    @Test
    void testWithdrawValid() {
        BankAccount acc = new BankAccount(INIT_BALANCE_100);
        acc.withdraw(WITHDRAW_40);
        Assertions.assertEquals(EXPECTED_60, acc.getBalance());
    }

    /**
     * Tests withdrawal that would cause overdraft.
     */
    @Test
    void testWithdrawOverdraft() {
        BankAccount acc = new BankAccount(INIT_BALANCE_30);
        Assertions.assertThrows(IllegalArgumentException.class, () -> acc.withdraw(WITHDRAW_40));
    }

    /**
     * Tests withdrawal with a negative amount.
     */
    @Test
    void testWithdrawNegative() {
        BankAccount acc = new BankAccount(INIT_BALANCE_30);
        Assertions.assertThrows(IllegalArgumentException.class, () -> acc.withdraw(NEGATIVE_5));
    }

    /**
     * Tests creating an account with a negative initial balance.
     */
    @Test
    void testNegativeInitialBalance() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BankAccount(NEGATIVE_100));
    }
}
