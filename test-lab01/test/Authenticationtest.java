import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Authenticationtest {
    private static final String LOGIN_URL = "https://automated-testing-year3.onrender.com/auth/login";

    private HttpResponse<String> sendLoginRequest(String email, String password) throws IOException, InterruptedException {
        HttpRequest http = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_URL))
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password)))
                .header("Content-Type", "application/json")
                .build();
        return HttpClient.newHttpClient().send(http, HttpResponse.BodyHandlers.ofString());
    }

    private void assertLoginStatus(String email, String password, int expectedStatus) throws IOException, InterruptedException {
        HttpResponse<String> response = sendLoginRequest(email, password);
        assertEquals(expectedStatus, response.statusCode());
    }

    @Test
    public void testLogin() throws IOException, InterruptedException {
        assertLoginStatus("john@mail.com", "changeme", 200);
    }

    @Test
    public void testLoginWithWrongPassword() throws IOException, InterruptedException {
        assertLoginStatus("john@mail.com", "wrongpassword", 401);
    }

    @Test
    public void testLoginWithWrongEmailFailed() throws IOException, InterruptedException {
        assertLoginStatus("a@mail.com", "changeme", 401);
    }

    @Test
    public void testLoginWithEmptyFields() throws IOException, InterruptedException {
        assertLoginStatus("", "", 400);
    }
}