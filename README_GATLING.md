# Lab 06 — Gatling Load Testing

End-to-end Gatling load-testing lab (Java DSL + Maven). Covers all 7 lab
exercises: scaffold → 3-step scenario → CSV feeder → load profile → assertions →
"break it" → CI.

> **Note on the target.** The classic public demo `computer-database.gatling.io`
> has been retired (no DNS A record as of 2026). Instead this project ships a
> tiny, self-contained backend — `automated.testing.ComputerDatabaseServer` —
> that serves the same shape of HTML pages, so the whole lab runs offline on
> `http://localhost:8080`. Point at any other host with `-DbaseUrl=...`.

## Project layout

| Path | Purpose |
|------|---------|
| `src/main/java/automated/testing/ComputerDatabaseServer.java` | Self-contained backend (JDK `HttpServer`, no deps) |
| `src/test/java/simulations/BasicSimulation.java` | Ex 1 — minimal "smoke test" simulation |
| `src/test/java/simulations/ComputerDatabaseSimulation.java` | Ex 2–6 — full load test |
| `src/test/resources/search-terms.csv` | Ex 3 — CSV feeder (one search term per VU) |
| `.github/workflows/gatling.yml` | Ex 7 — CI workflow that runs the test and uploads the report |

## How to run

**1. Start the backend** (leave it running in its own terminal):

```bash
mvn compile exec:java
# -> Computer Database backend listening on http://localhost:8080/computers
```

**2. Run a simulation** (in a second terminal):

```bash
# Ex 1 — smoke test
mvn -Dgatling.simulationClass=simulations.BasicSimulation gatling:test

# Ex 2–5 — full load test with default profile (ramp to 100 over 30s, hold 2 min)
mvn -Dgatling.simulationClass=simulations.ComputerDatabaseSimulation gatling:test
```

The HTML report is written to
`target/gatling/<simulation>-<timestamp>/index.html` — open it to read the
percentiles, throughput, active-users, and error charts.

## Exercise mapping

- **Ex 1 — Scaffold + sample:** `BasicSimulation` (protocol → scenario → setup,
  one user once).
- **Ex 2 — 3-step scenario with checks:** `ComputerDatabaseSimulation` does
  **list → search → detail**. The backend serves HTML, so checks use
  `substring`/`regex` (not `jsonPath`):
  - *List* `/computers` — `status 200`, page contains "Computers database",
    and a `/computers/<id>` link is captured into the session via
    `regex(...).findRandom().saveAs("computerId")`.
  - *Search* `/computers?f=#{searchTerm}` — `status 200`.
  - *Detail* `/computers/#{computerId}` — `status 200`, page contains "Computer".
- **Ex 3 — CSV feeder:** `csv("search-terms.csv").circular()` so each virtual
  user searches a different term.
- **Ex 4 — Load profile:** `rampUsers(100).during(30s)` then
  `constantUsersPerSec(rps).during(120s)` (hold ≈ 2 min).
- **Ex 5 — Assertions (SLA gates):**
  `global().responseTime().percentile3().lt(800)` (p95 < 800ms) **and**
  `global().successfulRequests().percent().gt(99.0)` (success > 99%).
  A failed assertion makes the Maven build exit non-zero.

## Ex 6 — Break it

The load is parametrized via `-D` system properties:

| Property | Default | Meaning |
|----------|---------|---------|
| `users`  | 100 | ramp target (Ex 4) |
| `ramp`   | 30  | ramp duration, seconds |
| `rps`    | 8   | hold injection rate, req/s |
| `hold`   | 120 | hold duration, seconds (= 2 min) |
| `baseUrl`| `http://localhost:8080` | target host |

Raising `rps` until an assertion fails (backend ceiling ≈ **530 req/s** — 16
worker threads × ~30 ms/request):

| Command (`...gatling:test`) | Actual throughput | Success rate | p95 | Verdict |
|---|---|---|---|---|
| `-Drps=8`   | 8 req/s   | 100 %  | 47 ms  | ✅ pass |
| `-Drps=100` | 184 req/s | 100 %  | 44 ms  | ✅ pass |
| `-Drps=300` | 510 req/s | **76.2 %** | 426 ms | ❌ **fail** |

**Failing point:** somewhere between ~185 and ~510 req/s of *delivered* load —
i.e. when demand approaches the backend's ~530 req/s capacity. At `rps=300` the
connection queue saturates and ~24 % of requests are rejected, so the
`successfulRequests > 99%` assertion fails and the build exits non-zero (255).
Interesting detail: when a `List` request fails, `computerId` is never saved, so
the dependent `View detail` step can't even build a request — a realistic
failure cascade.

Reproduce the break:

```bash
mvn -Dgatling.simulationClass=simulations.ComputerDatabaseSimulation gatling:test \
    -Dusers=50 -Dramp=2 -Drps=300 -Dhold=15
```

## Ex 7 — CI

`.github/workflows/gatling.yml` runs the full simulation on push/PR and uploads
`target/gatling/**` as the `gatling-report` artifact (`if: always()`, so the
report is published even when the SLA assertions fail and break the build).

## Windows / PowerShell note

PowerShell mangles unquoted `-D` arguments. Quote each one:

```powershell
mvn gatling:test "-Dgatling.simulationClass=simulations.ComputerDatabaseSimulation" "-Drps=300"
```
