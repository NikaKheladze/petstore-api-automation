# Swagger Petstore — API Test Automation

BDD test automation for the [Swagger Petstore](https://petstore.swagger.io/) REST API, built with
**Java 17, Maven, RestAssured, Cucumber (Gherkin), JUnit Platform** and **Allure** reporting.

---

## 1. Project Overview

The suite covers the **full CRUD lifecycle of the `Pet` resource** plus search, with positive and
negative scenarios. Every scenario validates **HTTP status codes, headers and response bodies**
(field values and JSON schema).

- Scenarios are written in **Gherkin** (`.feature` files) and read as business behaviour.
- Step definitions call the API through **RestAssured** via a small client layer.
- Every request/response is attached to the **Allure report**; a **Cucumber HTML report** is also produced.
- The suite is **safe to run against the shared public server**: unique test data, read-after-write
  polling, and automatic cleanup of everything it creates.

## 2. Technologies

| Area | Technology | Version |
|---|---|---|
| Language / build | Java, Maven (wrapper included) | 17+, 3.9.16 |
| HTTP / API testing | RestAssured (+ JSON Schema Validator) | 6.0.1 |
| BDD | Cucumber JVM (Gherkin, PicoContainer DI) | 7.34.9 |
| Test platform | JUnit Platform Suite | 5.14.4 |
| JSON mapping | Jackson | 2.22.3 |
| Assertions | RestAssured/Hamcrest matchers, AssertJ | 3.27.7 |
| Polling | Awaitility | 4.3.0 |
| Reporting | Allure (Cucumber + RestAssured adapters), Cucumber HTML | 2.35.5 |

## 3. Prerequisites

- **JDK 17 or newer** (`java -version`; `JAVA_HOME` set)
- **Internet access** to `https://petstore.swagger.io`
- Maven is **optional** — use the included wrapper `./mvnw` (Windows: `mvnw.cmd`)

No API keys, accounts or other tools are needed.

## 4. Setup

```bash
git clone <this-repo-url>
cd petstore-api-automation
./mvnw -q test-compile        # downloads dependencies and compiles (Windows: mvnw.cmd -q test-compile)
```

## 5. Running Tests

```bash
./mvnw clean test                                        # all scenarios
./mvnw clean test -Dcucumber.filter.tags="@smoke"        # by tag
./mvnw clean test -Dcucumber.filter.tags="@negative"
./mvnw clean test -Dcucumber.filter.tags="@crud and not @delete"
./mvnw clean test -Dcucumber.filter.name="Delete an existing pet"   # by scenario name
```

On **Windows PowerShell** use `.\mvnw.cmd` and quote `-D` arguments:

```powershell
.\mvnw.cmd clean test "-Dcucumber.filter.tags=@smoke"
```

Available tags: `@pet`, `@crud`, `@create`, `@read`, `@update`, `@delete`, `@search`, `@negative`, `@smoke`.

A failing scenario fails the Maven build; reports are written either way.

## 6. Reporting

**Allure report** (recommended — includes every HTTP request/response per step):

```bash
./mvnw allure:report     # -> target/site/allure-maven-plugin/index.html (single self-contained file)
./mvnw allure:serve      # generate and open in the browser
```

The first run downloads the Allure runtime into `.allure/` (git-ignored). If the download is blocked
(e.g. corporate proxy), use the Java-based Allure 2: `./mvnw allure:report -Dreport.version=2.36.0`.

**Cucumber HTML report** (no extra step): `target/cucumber-reports/cucumber.html`

**Console:** readable step-by-step output; for any failed RestAssured validation the full request and
response are printed.

## 7. Configuration

Defaults are in [`src/test/resources/config/config.properties`](src/test/resources/config/config.properties).
Each key is resolved in this order (first defined value wins): **`-D` system property → environment
variable → `config.local.properties` (git-ignored) → `config.properties`**.

| Property | Env variable | Default | Description |
|---|---|---|---|
| `petstore.base.url` | `PETSTORE_BASE_URL` | `https://petstore.swagger.io` | API host |
| `petstore.base.path` | `PETSTORE_BASE_PATH` | `/v2` | API base path |
| `petstore.api.key` | `PETSTORE_API_KEY` | `special-key` | `api_key` header (public demo key, not a secret) |
| `consistency.timeout.seconds` | `CONSISTENCY_TIMEOUT_SECONDS` | `15` | Max wait for a write to become visible |
| `consistency.poll.interval.millis` | `CONSISTENCY_POLL_INTERVAL_MILLIS` | `500` | Poll interval for read-after-write checks |

Run against a local Petstore instead of the public one:

```bash
docker run -d -p 8080:8080 swaggerapi/petstore
./mvnw clean test -Dpetstore.base.url=http://localhost:8080
```

## 8. Project Structure

```
├── src/main/java/com/petstore/api/
│   ├── config/   Config                    # layered configuration
│   ├── client/   RequestSpecFactory        # base URI, JSON, api_key, logging, Allure filter
│   │             PetClient                 # one method per /pet endpoint, returns raw responses
│   └── model/    Pet, Category, Tag, ApiMessage   # Jackson-mapped records
└── src/test/
    ├── java/com/petstore/api/
    │   ├── runner/   RunCucumberTest       # JUnit Platform suite executing all features
    │   ├── steps/    PetSteps              # pet actions and pet-specific checks
    │   │             ResponseSteps         # status, headers, JSON schema, error bodies
    │   │             CleanupHooks          # deletes every pet a scenario created
    │   ├── context/  ScenarioContext       # per-scenario state (PicoContainer-injected)
    │   ├── data/     TestPets              # unique test data
    │   └── support/  Eventually            # bounded read-after-write polling (Awaitility)
    └── resources/
        ├── features/pet/   pet_crud.feature, pet_negative.feature, find_by_status.feature
        ├── schemas/        pet.json, api-response.json
        ├── config/         config.properties (+ .example)
        └── junit-platform.properties, allure.properties
```

## 9. Test Coverage

**15 scenarios** (Scenario Outline examples counted individually): 5 CRUD, 6 negative, 4 search.

| Feature | Scenario | Validates |
|---|---|---|
| **CRUD** | Create a new pet | 200, JSON content type, schema, body = request, pet readable afterwards |
| | Retrieve an existing pet | 200, JSON, `Access-Control-Allow-Origin: *` header, schema, body |
| | Update an existing pet (PUT) | 200, body = updated pet, change visible on read |
| | Update a pet with form data | 200, `ApiResponse` schema, message = pet id, change visible on read |
| | Delete an existing pet | 200, `ApiResponse` schema, message = pet id, pet gone (404) |
| **Negative** | Retrieve a non-existent pet | 404, JSON, schema, `{code:1, type:"error", message:"Pet not found"}` |
| | Delete a non-existent pet | 404 |
| | Form-update a non-existent pet | 404, `{code:404, type:"unknown", message:"not found"}` |
| | Retrieve with non-numeric id (`abc`, `1.5`) | 404, JSON, error code 404 |
| | Add a pet with malformed JSON | 400, `{code:400, type:"unknown", message:"bad input"}` |
| **Search** | Find by status (`available`, `pending`, `sold`) | 200, every result has that status, created pet included |
| | Find by a status no pet has | 200, empty list |

## 10. Design Decisions

- **Cucumber on the JUnit Platform.** A `@Suite` runner is discovered by Maven Surefire; Cucumber options
  live in `junit-platform.properties` so any of them (e.g. tags) can be overridden with `-D`.
- **Thin client + step layers.** `PetClient` only sends requests and returns raw `Response`s; all
  expectations live in step definitions. That keeps negative tests natural (any status can be asserted)
  and keeps endpoint details out of the steps.
- **Dependency injection with PicoContainer.** Step classes share a per-scenario `ScenarioContext` via
  constructor injection — no static state, so scenarios are independent.
- **Status, headers and body are validated separately and explicitly**: status via RestAssured, body
  field-by-field via AssertJ recursive comparison against the expected model, structure via JSON Schema.
- **Built for a shared public server.**
  - *Unique data:* every pet gets a random 13-digit id, so runs never collide with other users' data.
  - *Read-after-write polling:* the public Petstore is load-balanced and occasionally serves a stale read
    right after a write. Checks that read back data poll with Awaitility (max 15 s, configurable) and fail
    with the last response in the message. No fixed sleeps.
  - *Cleanup:* an `@After` hook deletes every pet the scenario created, even when the scenario fails.
- **Assert the real behaviour, document the spec gaps** (verified against the server's source code):

  | Case | Swagger definition | Actual server |
  |---|---|---|
  | Invalid (non-numeric) id | 400 | 404 |
  | Malformed JSON body | 405 | 400 `bad input` |
  | Delete a missing pet | 404 | 404 with an empty body |
  | PUT a missing pet | 404 | 200 — PUT is an upsert, so there is no "update missing pet" negative test |

- **Versions.** Cucumber stays on the mature 7.x line because the Allure Cucumber adapter targets the
  Cucumber 7 plugin API (Cucumber 8.0 was released only days before this project). RestAssured 6.0.1
  matches the version the Allure RestAssured adapter is built against; JUnit 5.14.x is the platform
  version Cucumber 7.34 is tested with.
