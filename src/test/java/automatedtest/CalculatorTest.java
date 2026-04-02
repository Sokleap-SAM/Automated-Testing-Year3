package automatedtest;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ICalculator.
 */
public class CalculatorTest {

    /** Constant value 3. */
    private static final double THREE = 3.0;

    /** Constant value 5. */
    private static final double FIVE = 5.0;

    /** Constant value 6. */
    private static final double SIX = 6.0;

    /**
     * Returns a calculator implementation for testing.
     */
    public ICalculator getCalculator() {
        return new Calculator();
    }

    /**
     * Tests addition.
     */
    @Test
    public void testAdd() {
        ICalculator calculator = getCalculator();
        double a = 2.0;
        double b = THREE;
        double expected = FIVE;
        assert calculator.add(a, b) == expected
            : "Addition test failed (Expected: " + expected + ", Actual: " + calculator.add(a, b) + ")";
    }

    /**
     * Tests subtraction.
     */
    @Test
    public void testSubtract() {
        ICalculator calculator = getCalculator();
        double a = FIVE;
        double b = THREE;
        double expected = 2.0;
        assert calculator.subtract(a, b) == expected
            : "Subtraction test failed (Expected: " + expected + ", Actual: " + calculator.subtract(a, b) + ")";
    }

    /**
     * Tests multiplication.
     */
    @Test
    public void testMultiply() {
        ICalculator calculator = getCalculator();
        double a = 2.0;
        double b = THREE;
        double expected = SIX;
        assert calculator.multiply(a, b) == expected
            : "Multiplication test failed (Expected: " + expected + ", Actual: " + calculator.multiply(a, b) + ")";
    }

    /**
     * Tests division.
     */
    @Test
    public void testDivide() {
        ICalculator calculator = getCalculator();
        double a = SIX;
        double b = THREE;
        double expected = 2.0;
        assert calculator.divide(a, b) == expected
            : "Division test failed (Expected: " + expected + ", Actual: " + calculator.divide(a, b) + ")";
    }

    /**
     * Tests modulo.
     */
    @Test
    public void testModulo() {
        ICalculator calculator = getCalculator();
        double a = FIVE;
        double b = THREE;
        double expected = 2.0;
        assert calculator.modulo(a, b) == expected
            : "Modulo test failed (Expected: " + expected + ", Actual: " + calculator.modulo(a, b) + ")";
    }

    /**
     * Tests division by zero throws ArithmeticException.
     */
    @Test
    public void testDivideByZero() {
        ICalculator calculator = getCalculator();
        try {
            calculator.divide(FIVE, 0);
            assert false : "Division by zero test failed (Expected: ArithmeticException)";
        } catch (ArithmeticException e) {
            assert e.getMessage().equals("Division by zero")
                : "Division by zero test failed (Actual: '" + e.getMessage() + "')";
        }
    }

    /**
     * Tests modulo by zero throws ArithmeticException.
     */
    @Test
    public void testModuloByZero() {
        ICalculator calculator = getCalculator();
        try {
            calculator.modulo(FIVE, 0);
            assert false : "Modulo by zero test failed (Expected: ArithmeticException)";
        } catch (ArithmeticException e) {
            assert e.getMessage().equals("Division by zero")
                : "Modulo by zero test failed (Actual: '" + e.getMessage() + "')";
        }
    }
}

