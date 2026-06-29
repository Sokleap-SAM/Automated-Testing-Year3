package automatedtesting.lab08.lab08.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * A dependency-free HTTP client for the API tests, built on {@code java.net.http}.
 * Avoids any reliance on Spring's test client or a particular Jackson version,
 * so the suite stays stable across Spring Boot module reshuffles.
 */
public final class Http {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private Http() {
    }

    public record Resp(int status, String body) {
    }

    public record BytesResp(int status, byte[] body) {
    }

    private static String basic(String email, String password) {
        String token = email + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private static String url(int port, String path) {
        return "http://localhost:" + port + path;
    }

    public static Resp get(int port, String path, String email, String password) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(port, path)))
                .header("Authorization", basic(email, password))
                .GET()
                .build();
        return send(req);
    }

    public static Resp postJson(int port, String path, String email, String password, String json) {
        return jsonRequest("POST", port, path, email, password, json);
    }

    public static Resp putJson(int port, String path, String email, String password, String json) {
        return jsonRequest("PUT", port, path, email, password, json);
    }

    public static Resp patchJson(int port, String path, String email, String password, String json) {
        return jsonRequest("PATCH", port, path, email, password, json);
    }

    public static Resp delete(int port, String path, String email, String password) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(port, path)))
                .header("Authorization", basic(email, password))
                .DELETE()
                .build();
        return send(req);
    }

    private static Resp jsonRequest(String method, int port, String path, String email,
                                    String password, String json) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(port, path)))
                .header("Authorization", basic(email, password))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(req);
    }

    public static BytesResp getBytes(int port, String path, String email, String password) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(port, path)))
                .header("Authorization", basic(email, password))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return new BytesResp(res.statusCode(), res.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Upload a single file as multipart/form-data under the field name "file". */
    public static Resp uploadFile(int port, String path, String email, String password,
                                  String filename, byte[] content) {
        String boundary = "----lab08-" + UUID.randomUUID();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        write(body, "--" + boundary + "\r\n");
        write(body, "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n");
        write(body, "Content-Type: application/octet-stream\r\n\r\n");
        writeBytes(body, content);
        write(body, "\r\n--" + boundary + "--\r\n");

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(port, path)))
                .header("Authorization", basic(email, password))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();
        return send(req);
    }

    private static Resp send(HttpRequest req) {
        try {
            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            return new Resp(res.statusCode(), res.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(ByteArrayOutputStream out, String s) {
        writeBytes(out, s.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeBytes(ByteArrayOutputStream out, byte[] bytes) {
        out.writeBytes(bytes);
    }
}
