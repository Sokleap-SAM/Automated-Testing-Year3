import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UserTest {
    @Test
    public void testChangeEmail(){
        User user = new User();
        assertNull(user.getEmail());
        user.setEmail("newemail@mail.com");
        assertEquals("newemail@mail.com", user.getEmail());
    }
}
