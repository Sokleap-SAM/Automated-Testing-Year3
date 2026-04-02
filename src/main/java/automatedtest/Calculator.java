package automatedtest;

/**
 * Calculator implementation.
 */
public class Calculator implements ICalculator {

    /**
     * Adds two numbers.
     * @param a first number
     * @param b second number
     * @return sum
     */
    @Override
    public double add(double a, double b) {
        return a + b;
    }

    /**
     * Subtracts b from a.
     * @param a first number
     * @param b second number
     * @return difference
     */
    @Override
    public double subtract(double a, double b) {
        return a - b;
    }

    /**
     * Multiplies two numbers.
     * @param a first number
     * @param b second number
     * @return product
     */
    @Override
    public double multiply(double a, double b) {
        return a * b;
    }

    /**
     * Divides a by b.
     * @param a first number
     * @param b second number
     * @return quotient
     * @throws ArithmeticException if b is zero
     */
    @Override
    public double divide(double a, double b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }

    /**
     * Returns remainder of a divided by b.
     * @param a first number
     * @param b second number
     * @return remainder
     * @throws ArithmeticException if b is zero
     */
    @Override
    public double modulo(double a, double b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a % b;
    }
}
