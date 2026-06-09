package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Lab Exercise 1 — the generated "smoke test" sample.
 *
 * Minimal Gatling simulation with the three building blocks:
 *   1) PROTOCOL  — shared HTTP config
 *   2) SCENARIO  — the user journey (a single GET here)
 *   3) SETUP     — injection profile + protocol binding
 *
 * Run: mvn -Dgatling.simulationClass=simulations.BasicSimulation gatling:test
 */
public class BasicSimulation extends Simulation {

    // 1) PROTOCOL — the backend serves HTML, so accept text/html.
    // Defaults to the local ComputerDatabaseServer; override with -DbaseUrl=...
    HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
            .acceptHeader("text/html,application/xhtml+xml")
            .userAgentHeader("Gatling/LoadTest");

    // 2) SCENARIO — list the computers
    ScenarioBuilder scn = scenario("Browse computers")
            .exec(http("List").get("/computers")
                    .check(status().is(200)));

    // 3) SETUP — one user, once
    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}
