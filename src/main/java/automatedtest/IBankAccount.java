package automatedtest;

/**
 * Interface representing the contract for Bank Account operations.
 */
public interface IBankAccount {
    /**
     * Returns the current balance.
     * @return the account balance
     */
    double getBalance();

    /**
     * Deposits a positive amount into the account.
     * @param amount the amount to deposit
     * @throws IllegalArgumentException if amount is not positive
     */
    void deposit(double amount);

    /**
     * Withdraws a positive amount from the account.
     * @param amount the amount to withdraw
     * @throws IllegalArgumentException if amount is not positive or insufficient funds
     */
    void withdraw(double amount);
}
