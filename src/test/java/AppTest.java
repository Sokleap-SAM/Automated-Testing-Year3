import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for App class
 */
public class AppTest {
    
    @Test
    public void testGreetMethod() {
        String result = App.greet("World");
        assertEquals("Hello, World!", result);
    }
    
    @Test
    public void testGreetWithDifferentName() {
        String result = App.greet("Jenkins");
        assertEquals("Hello, Jenkins!", result);
    }
}
