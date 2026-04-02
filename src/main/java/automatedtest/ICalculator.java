package automatedtest;

/**
 * Interface for the ICalculator.
 */
public interface ICalculator {

    /**
     * Adds two numbers.
     * @param a first number
     * @param b second number
     * @return sum
     */
    double add(double a, double b);

    /**
     * Subtracts b from a.
     * @param a first number
     * @param b second number
     * @return difference
     */
    double subtract(double a, double b);

    /**
     * Multiplies two numbers.
     * @param a first number
     * @param b second number
     * @return product
     */
    double multiply(double a, double b);

    /**
     * Divides a by b.
     * @param a first number
     * @param b second number
     * @return quotient
     * @throws ArithmeticException if b is zero
     */
    double divide(double a, double b) throws ArithmeticException;

    /**
     * Returns remainder of a divided by b.
     * @param a first number
     * @param b second number
     * @return remainder
     * @throws ArithmeticException if b is zero
     */
    double modulo(double a, double b) throws ArithmeticException;
}
