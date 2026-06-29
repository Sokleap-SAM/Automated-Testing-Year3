# Private Cloud Storage

A Spring Boot REST API (with a thin Thymeleaf UI) for a personal cloud drive.
Each registered user gets an isolated **50 MB** storage space and can only ever
see their own data.

> **Hard rule:** a user must never read, write or delete another user's files.
> Enforcing *and testing* this isolation is a first-class goal of the project.

---

## Features

| #  | Capability     | Detail                                             |
|----|----------------|----------------------------------------------------|
| R1 | Register       | New user created with a 50 MB quota                 |
| R2 | Authenticate   | HTTP Basic login; each request acts as that user   |
| R3 | Manage profile | View & update display name / password              |
| R4 | Delete account | User deletes own account + all their data          |
| R5 | Folders        | Create, rename, move, list, delete folders         |
| R6 | Files          | Upload, download, rename, move, delete files       |
| R7 | Quota          | Reject uploads that exceed remaining space (413)   |
| R8 | Isolation      | No access to another user's files/folders (404)    |

## Architecture

```
controller/   REST endpoints (@RestController) + global exception handler
service/      business logic: quota, isolation, formulas
repository/   Spring Data JPA (User, Folder, FileEntity)
model/        JPA entities
dto/          request/response records
config/       Spring Security (HTTP Basic + form login) + current-user resolver
web/          Thymeleaf UI: signup, login, dashboard (upload + file list)
```

* **Storage** — H2 in-memory DB; file bytes are kept in a BLOB so `usedBytes`
  stays in sync and the quota formula `free = quota − Σ(file sizes)` is testable.
* **Auth** — two mechanisms over the same users:
  * **HTTP Basic** for the REST API (used by `curl` and the JUnit HTTP tests).
  * **Session-based form login** for the browser UI (`/signup`, `/login`, `/logout`).

  Register/login are public; everything else is authenticated and scoped to the
  logged-in user.

---

## How to run the app

```bash
mvn spring-boot:run
```

The API is then at `http://localhost:8080`. For the browser UI, open
`http://localhost:8080/signup` to create an account, then you're taken to
`http://localhost:8080/login`; after signing in you land on the dashboard
(quota + file list + upload form).

Quick smoke test with `curl`:

```bash
# register (granted 50 MB)
curl -X POST localhost:8080/api/auth/register \
     -H 'Content-Type: application/json' \
     -d '{"email":"sok@itc.edu","password":"Secret123!"}'

# view profile + quota usage (HTTP Basic)
curl -u sok@itc.edu:Secret123! localhost:8080/api/me

# upload a file
curl -u sok@itc.edu:Secret123! -F file=@./notes.txt localhost:8080/api/files
```

### API surface

```
# Accounts
POST   /api/auth/register      {email,password} -> user (50 MB)
POST   /api/auth/login         verify credentials
GET    /api/me                 profile + quota usage
PUT    /api/me                 update display name / password
DELETE /api/me                 delete own account + data

# Folders
POST   /api/folders            GET /api/folders?parent=ID
PATCH  /api/folders/{id}       DELETE /api/folders/{id}

# Files
POST   /api/files (multipart)  GET  /api/files/{id}/download
GET    /api/files?folder=ID    PATCH /api/files/{id}
DELETE /api/files/{id}
```

---

## How to run the tests

```bash
mvn test
```

This runs the JUnit + Playwright suite and writes Allure results to
`target/allure-results`.

> **Playwright note:** the visual test drives a real Chromium browser.
> Playwright for Java downloads the browser automatically on first run. If the
> browser cannot be provisioned (offline machine), that single test *self-skips*
> so the rest of the suite still passes. To force-install the browser:
> `mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`.

## How to open the Allure report

Easiest — build the report, start a local web server and open it in the browser:

```bash
mvn allure:serve
```

> **Note:** don't just double-click `target/site/allure-maven-plugin/index.html` —
> a static Allure report loads its data via JavaScript, which browsers block over
> `file://`, so it shows up blank. Serve it over HTTP instead (`allure:serve`).
> The per-test **screenshots live under each test → Attachments** in the report.

To only generate the static report (e.g. to upload as a CI artifact):

```bash
mvn allure:report   # -> target/site/allure-maven-plugin
```

…or, if you have the standalone Allure CLI installed:

```bash
allure serve target/allure-results
```

Tests are grouped by `@Feature` so each testing method is easy to find.

The Playwright UI tests also save their screenshots to the **`images/`** folder
(as well as attaching them to the Allure report):

| File | Captured by |
|------|-------------|
| `images/dashboard-quota.png`       | dashboard showing the 50 MB quota |
| `images/dashboard-after-upload.png`| file listed after a browser upload |
| `images/auth-after-login.png`      | dashboard right after form login |
| `images/auth-login-error.png`      | rejected login (wrong password) |
| `images/auth-after-logout.png`     | login page after signing out |

---

## Testing methods → where they are demonstrated

All ten methods from the lecture are exercised against real scenarios:

| Method            | Demonstrated by (test → assertion)                                                                                   |
|-------------------|----------------------------------------------------------------------------------------------------------------------|
| Content equals    | `QuotaServiceTest.newUserGets50MbAndFormulaHolds` — `quotaBytes == 50 MB`; `FilesApiTest.fileContentAndIdRoundTrip` — bytes & id round-trip |
| Contains          | `ProfileApiTest.profileEndpointContract` — folders contains `"Documents"`; `FilesApiTest.listingContainsUploadedFileName` |
| Regex matched     | `ProfileApiTest.profileEndpointContract` — email matches `^[\w.+-]+@[\w.-]+$`; `FilesApiTest.downloadLinkMatchesRegex` — `/api/files/\d+/download` |
| Formula matched   | `QuotaServiceTest.newUserGets50MbAndFormulaHolds` — `free = quota − Σ(file sizes)`                                  |
| Predicate         | `QuotaServiceTest.usedBytesNeverExceedsQuota`; `IsolationTest.usersCannotSeeEachOther` — `b uses nothing`           |
| Collection        | `FilesApiTest.listingIsOrderedSizedAndDeduplicated` — size, ordering, no duplicates; `IsolationTest` empty listing  |
| Exception         | `QuotaServiceTest` — `QuotaExceededException`; `FilesApiTest.overQuotaUploadIsRejected` — HTTP 413                  |
| Tolerance         | `QuotaServiceTest.usedMegabytesAreWithinTolerance` — `≈ 25.0` within δ; `ProfileApiTest.profileRespondsWithinBudget` — response < 2 s |
| Schema/JSON       | `ProfileApiTest.profileEndpointContract` — `/api/me` shape & fields                                                  |
| Visual/snapshot   | `DashboardVisualTest` — Playwright web assertion + screenshot (quota + file upload flow); `AuthUiTest` — signup/login/logout flows |

## Requirements → tests

Every test is named `Rx — …` (via `@DisplayName`) so each requirement is easy to
find in the test output and the Allure report.

| Req | Tests (each tagged `Rx — …` in `@DisplayName`) |
|-----|------------------------------------------------|
| R1 Register (50 MB) | `QuotaServiceTest` (R1/R7), `AuthUiTest.signUpThenLogIn` (R1/R2) |
| R2 Authenticate     | `AuthUiTest` — sign up + login, wrong password, logout; `DashboardVisualTest` (login) |
| R3 Manage profile   | `ProfileApiTest` — GET contract, response budget, **PUT update name & password** |
| R4 Delete account   | `ProfileApiTest.deleteAccountRemovesEverything` |
| R5 Folders          | `FolderApiTest` — create/list, rename, move, delete |
| R6 Files            | `FilesApiTest` — contains, collection, round-trip, regex; `DashboardVisualTest` upload |
| R7 Quota            | `QuotaServiceTest` (formula/tolerance/predicate), `FilesApiTest.overQuotaUploadIsRejected` (413) |
| R8 Isolation        | `IsolationTest`, `FilesApiTest.cannotDownloadAnotherUsersFile` (404), `FolderApiTest.cannotTouchAnotherUsersFolder` (404) |

### Test suites at a glance

| Suite                  | `@Feature`  | Focus                                  |
|------------------------|-------------|----------------------------------------|
| `QuotaServiceTest`     | Quota       | quota maths, formula, exceptions (R1/R7) |
| `IsolationTest`        | Isolation   | R8 — users can't see each other        |
| `ProfileApiTest`       | Profile     | `/api/me` contract, update, delete (R3/R4) |
| `FilesApiTest`         | Files       | upload/list/download, quota, isolation (R6/R7/R8) |
| `FolderApiTest`        | Folders     | create/rename/move/delete + isolation (R5/R8) |
| `DashboardVisualTest`  | Dashboard   | Playwright: quota snapshot + file upload flow |
| `AuthUiTest`           | Authentication | Playwright: signup, login, bad-login, logout |

All 26 tests pass.

---

## Project layout

```
private-cloud-storage/
├── src/main/java/...            # Spring Boot app
├── src/main/resources/          # templates + static + application.properties
├── src/test/java/...            # JUnit + Playwright tests
├── target/allure-results/       # raw Allure results (after `mvn test`)
├── .github/workflows/ci.yml     # build + test + report
├── pom.xml
└── README.md
```
