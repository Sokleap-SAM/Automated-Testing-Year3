package simulations;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Lab Exercises 2-6 — full load test against computer-database.gatling.io.
 *
 *   Ex 2: 3-step user journey  list -> search -> detail, with checks.
 *   Ex 3: CSV feeder so each virtual user searches a different term.
 *   Ex 4: load profile  ramp to 100 users over 30s, then hold for 2 min.
 *   Ex 5: assertions     p95 < 800ms  AND  success rate > 99%.
 *   Ex 6: "break it"     load is parametrized via -D system properties so the
 *                        load can be cranked up until an assertion fails.
 *
 * NOTE: computer-database.gatling.io serves HTML (not JSON), so the checks use
 *       regex / substring against the markup, not jsonPath.
 *
 * Run (defaults):     mvn -Dgatling.simulationClass=simulations.ComputerDatabaseSimulation gatling:test
 * Run (break it):     mvn -Dgatling.simulationClass=simulations.ComputerDatabaseSimulation gatling:test -Dusers=100 -Dramp=30 -Drps=120 -Dhold=120
 */
public class ComputerDatabaseSimulation extends Simulation {

    // --- Tunable load parameters (Ex 6) -------------------------------------
    private static int prop(String name, int def) {
        return Integer.parseInt(System.getProperty(name, String.valueOf(def)));
    }

    // ramp to USERS over RAMP seconds, then hold RPS req/s for HOLD seconds
    private static final int USERS = prop("users", 100); // Ex 4: ramp target
    private static final int RAMP  = prop("ramp", 30);   // Ex 4: ramp duration (s)
    private static final int RPS   = prop("rps", 8);     // hold injection rate (req/s)
    private static final int HOLD  = prop("hold", 120);  // Ex 4: hold duration (s) = 2 min

    // Target: defaults to the local backend (automated.testing.ComputerDatabaseServer).
    // Point at any host with -DbaseUrl=https://computer-database.gatling.io
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    // --- 1) PROTOCOL --------------------------------------------------------
    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .userAgentHeader("Gatling/LoadTest")
            .shareConnections()
            .check(status().not(500)); // global guard

    // --- 2) FEEDER (Ex 3) ---------------------------------------------------
    // Each VU pulls a different search term; .circular loops back when exhausted.
    FeederBuilder<String> searchFeeder = csv("search-terms.csv").circular();

    // --- 3) SCENARIO: list -> search -> detail (Ex 2) -----------------------
    ScenarioBuilder scn = scenario("Search and view computers")
            // Step 1: LIST — load the catalog and capture a real computer id
            .exec(http("List computers").get("/computers")
                    .check(status().is(200))
                    .check(substring("Computers database").exists())
                    // grab any /computers/<id> link from the listing for the detail step
                    .check(regex("/computers/(\\d+)").findRandom().saveAs("computerId")))
            .pause(Duration.ofSeconds(2)) // think time
            // Step 2: SEARCH — each VU searches its own feeder term
            .feed(searchFeeder)
            .exec(http("Search #{searchTerm}").get("/computers?f=#{searchTerm}")
                    .check(status().is(200)))
            .pause(1, 3) // random 1-3s think time
            // Step 3: DETAIL — view the computer captured from the listing
            .exec(http("View detail").get("/computers/#{computerId}")
                    .check(status().is(200))
                    .check(substring("Computer").exists()));

    // --- 4) SETUP: injection profile + assertions (Ex 4 + 5) ----------------
    {
        setUp(
                scn.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP)),  // ramp to 100 over 30s
                        constantUsersPerSec(RPS).during(Duration.ofSeconds(HOLD)) // hold for 2 min
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(800),   // Ex 5: p95 < 800ms
                        global().successfulRequests().percent().gt(99.0)  // Ex 5: success > 99%
                );
    }
}
