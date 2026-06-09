package automated.testing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Self-contained "Computer Database" backend for the Gatling lab.
 *
 * The classic public demo (computer-database.gatling.io) has been retired, so
 * this is a stand-in that serves the same shape of HTML pages the simulation
 * expects:
 *
 *   GET /computers            -> HTML listing, links like <a href="/computers/12">
 *   GET /computers?f=term     -> filtered listing (case-insensitive substring)
 *   GET /computers/{id}       -> detail page containing the word "Computer"
 *
 * It is deliberately resource-constrained (small thread pool + bounded queue +
 * a little per-request latency) so that:
 *   - at modest load the SLA passes (p95 < 800ms, success > 99%), and
 *   - cranking the load up enough saturates it and makes assertions FAIL,
 *     which is exactly what lab Exercise 6 ("break it") asks for.
 *
 * Run:  mvn -q compile exec:java   (see pom.xml exec-maven-plugin config)
 *   or: java -cp target/classes automated.testing.ComputerDatabaseServer [port]
 */
public class ComputerDatabaseServer {

    private static final String[] NAMES = {
            "MacBook Pro", "Apple IIe", "Commodore 64", "Atari 800", "Amiga 500",
            "Acorn Archimedes", "Sega Genesis", "Sony Vaio", "Amstrad CPC", "Nintendo Famicom",
            "IBM PC", "ZX Spectrum", "BBC Micro", "TRS-80", "Tandy 1000",
            "NeXT Cube", "Compaq Portable", "Dell Dimension", "HP Pavilion", "Asus Zenbook"
    };

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        // Small, bounded pool: the bottleneck that "break it" will saturate.
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                16, 16, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 100);
        server.createContext("/computers", ComputerDatabaseServer::handleComputers);
        server.createContext("/", ComputerDatabaseServer::handleRoot);
        server.setExecutor(pool);
        server.start();
        System.out.println("Computer Database backend listening on http://localhost:" + port + "/computers");
    }

    private static void handleRoot(HttpExchange ex) throws IOException {
        // redirect bare "/" to the listing, like the original app
        ex.getResponseHeaders().add("Location", "/computers");
        respond(ex, 303, "");
    }

    private static void handleComputers(HttpExchange ex) throws IOException {
        simulateWork();
        String path = ex.getRequestURI().getPath();

        // /computers/{id} -> detail
        if (path.matches("/computers/\\d+")) {
            int id = Integer.parseInt(path.substring("/computers/".length()));
            if (id < 0 || id >= NAMES.length) {
                respond(ex, 404, html("Not found", "<h1>Computer not found</h1>"));
                return;
            }
            String body = html("Computer detail",
                    "<h1>Computer #" + id + "</h1>"
                            + "<p>Name: " + NAMES[id] + "</p>"
                            + "<a href=\"/computers\">Back to list</a>");
            respond(ex, 200, body);
            return;
        }

        // /computers (optionally ?f=term) -> listing
        String filter = queryParam(ex, "f");
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            if (filter == null || NAMES[i].toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) {
                ids.add(i);
            }
        }
        String rows = ids.stream()
                .map(i -> "<tr><td><a href=\"/computers/" + i + "\">" + NAMES[i] + "</a></td></tr>")
                .collect(Collectors.joining());
        String body = html("Computers database",
                "<h1>Computers database</h1>"
                        + "<p>" + ids.size() + " computers found</p>"
                        + "<table>" + rows + "</table>");
        respond(ex, 200, body);
    }

    /** A little work + jitter so response times are realistic and saturate under load. */
    private static void simulateWork() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(15, 45));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String queryParam(HttpExchange ex, String key) {
        String q = ex.getRequestURI().getRawQuery();
        if (q == null || q.isEmpty()) return null;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv[0].equals(key) && kv.length == 2) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static String html(String title, String content) {
        return "<!DOCTYPE html><html><head><title>" + title + "</title></head><body>"
                + content + "</body></html>";
    }

    private static void respond(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            ex.close();
        }
    }
}
