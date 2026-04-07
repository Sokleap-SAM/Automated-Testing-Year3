package automatedtest;

/**
 * A concrete implementation of IBankAccount.
 */
public class BankAccount implements IBankAccount {
    private static final double MIN_BALANCE = 0.0;

    private double balance;

    /**
     * Constructs a BankAccount with an initial balance.
     * @param initialBalance the starting balance, must not be negative
     * @throws IllegalArgumentException if initialBalance is negative
     */
    public BankAccount(double initialBalance) {
        if (initialBalance < MIN_BALANCE) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance - amount < MIN_BALANCE) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance -= amount;
    }
}
