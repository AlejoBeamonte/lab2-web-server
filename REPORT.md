# Lab 2 Web Server -- Project Report

## What I specified

Before implementation, I specified three server behaviours.

1. Requests without a handler must return a custom HTML error page instead of the Spring Boot whitelabel page. I would verify this by requesting an unknown path as HTML and checking for a 404 response and custom page content.

2. `GET /time` must return the current server time as JSON. The controller must obtain the time through an interface, so a test can use a fixed time. I would verify a 200 response, JSON content, and a `time` property.

3. The application must use a self-signed localhost certificate, HTTPS on port 8443, and HTTP/2 enabled in Spring Boot. I would verify HTTPS responses for the error page and `/time`, and inspect HTTP/2 negotiation with an HTTP/2-capable client.

4. For the accepted bonus, `GET /time` must use the same URI for JSON, XML, and HTML representations selected through `Accept`. It must localize text values through `Accept-Language`, support transparent gzip compression through `Accept-Encoding`, and support conditional requests with ETag/If-None-Match and Last-Modified/If-Modified-Since. I would verify both successful representations and `406 Not Acceptable`, both languages, gzip headers, and `304 Not Modified` responses.

## What I changed

- Added `src/main/resources/templates/error.html`. It is a Thymeleaf error template with a custom marker and the error `status` and request `path`.

- Added `src/test/kotlin/es/unizar/webeng/lab2/ErrorPageTest.kt`. It starts a real server on a random port and uses `TestRestTemplate` to verify the rendered 404 HTML response.

- Added `src/main/kotlin/es/unizar/webeng/lab2/TimeComponent.kt`. It contains `TimeDTO`, the `TimeProvider` interface, `TimeService`, and the `LocalDateTime.toDTO()` extension.

- Added `src/main/kotlin/es/unizar/webeng/lab2/TimeController.kt`. It exposes `GET /time` and delegates to `TimeProvider`.

- Added `src/test/kotlin/es/unizar/webeng/lab2/TimeControllerTest.kt`. It uses `MockMvc` and a fixed primary `TimeProvider` to verify the JSON response and an exact timestamp.

- Added `src/main/resources/localhost.p12`, the PKCS#12 keystore used at runtime.

- Added `src/main/resources/application.yml` to enable TLS on port 8443, configure the keystore and alias, and enable HTTP/2.

- Added `src/test/resources/application.yml` to disable SSL for tests.

- Created the ignored local files `openssl-localhost.cnf`, `localhost.crt`, and `localhost.key` to generate the keystore. They are intentionally not tracked; only `localhost.p12` is part of the repository.

- Added the bonus on the `bonus-time-http-negotiation` branch. `TimeViewConfiguration.kt` configures Spring MVC content negotiation with Jackson JSON, Jackson XML, and Thymeleaf views. `GET /time` remains the only URI for all representations.

- Added `tools.jackson.dataformat:jackson-dataformat-xml`, `templates/time.html`, and content-negotiation tests for JSON, XML, HTML, and an unsupported PDF request.

- Added `messages.properties`, `messages_en.properties`, and `messages_es.properties`. The `TimeDTO` now contains stable `label` and `time` properties; only the value of `label` is translated.

- Enabled Tomcat response compression in the main and test application configuration. Added `CompressionTest.kt`, which uses a real server and checks that a request with `Accept-Encoding: gzip` receives `Content-Encoding: gzip`.

- Added a minute-based `TimeSnapshotService`. It creates one exact-time snapshot for each minute, then `TimeController` uses Spring MVC's `WebRequest.checkNotModified` with its ETag and Last-Modified values. The controller tests cover both ETag and date-based `304 Not Modified` responses.

## Technical decisions

- I used Thymeleaf's conventional `error.html` template instead of defining a custom error controller. This keeps Spring Boot's error handling and only replaces the HTML view.

- The error page shows the status and path, rather than only a generic message, to make a failed request easier to identify.

- I used `TestRestTemplate` with a real web server for the error page because `MockMvc` returns the 404 but does not render the error template.

- I kept the time domain/service code in `TimeComponent.kt` and the HTTP controller in `TimeController.kt`. The controller depends on `TimeProvider`, not on `LocalDateTime.now()` directly.

- The time test provides a fixed primary `TimeProvider`. This makes the timestamp assertion deterministic while the production service still returns the real server time.

- The certificate contains both `DNS:localhost` and `IP:127.0.0.1` as subject alternative names. The PKCS#12 entry alias is `localhost` and is configured explicitly in Spring Boot.

- TLS remains enabled in the main application configuration, but disabled in test resources. This keeps controller and error-page tests on plain HTTP and avoids making their test clients trust a self-signed certificate.

- I kept the Git history readable by using feature branches and small logical commits for the three implementation tasks.

- For the bonus, I changed the controller from a REST controller returning a body directly to an MVC controller returning one logical view and one common model. `ContentNegotiatingViewResolver` selects the JSON, XML, or Thymeleaf representation, avoiding three duplicated endpoints or controller methods.

- I used Spring's `MessageSource` and the `Locale` resolved from `Accept-Language`. The JSON and XML property names remain `label` and `time`; localization changes only `label` values. Explicit English and Spanish bundles avoid depending on the Windows system locale.

- I used Spring Boot/Tomcat server compression instead of compressing bytes in application code. The minimum response size is `1B` only so that the small `/time` response can demonstrate gzip in this laboratory.

- A continuously changing clock cannot honestly return `304`. I therefore modelled `/time` as a snapshot: the first request in each minute captures the displayed server time, and the same snapshot is reused for the rest of that minute. Its `Last-Modified` value is the snapshot creation time rounded to HTTP date precision. The weak ETag identifies the snapshot and locale. A weak validator is appropriate because JSON, XML, HTML, and gzip have different bytes but the same snapshot meaning.

- The response contains `Vary: Accept, Accept-Language, Accept-Encoding` so caches keep separate representations and languages. I disabled the default no-store setting in the Jackson views because a no-store response cannot be revalidated conditionally.

- I kept the bonus history as four focused commits: `Add content-negotiated time views`, `Add localized time representation`, `Enable response compression`, and `Add conditional time validation`.

## How I verified

After cloning the starter repository, I ran:

```bash
./gradlew.bat check
```

The first execution failed because Ktlint reported a multiline-expression formatting problem in `build.gradle.kts`.

I fixed the formatting with:

```bash
./gradlew.bat ktlintFormat
```

and reran the checks successfully.

During development I ran:

```bash
./gradlew.bat test ktlintCheck
```

The tests and style checks completed with `BUILD SUCCESSFUL`.

After completing the three required tasks I ran:

```bash
./gradlew.bat check
```

and the complete project validation finished successfully.

I started the application with:

```bash
./gradlew.bat bootRun
```

Tomcat started using HTTPS on port 8443.

During one verification attempt, the server could not start because port 8443 was already being used by a previous Java process. I identified and stopped the process and then restarted the application successfully.

The default Windows curl did not support HTTP/2. I confirmed this with:

```bash
curl.exe -V
```

Its feature list did not contain `HTTP2`.

I therefore used curl 8.22.0 with `nghttp2` support. Its feature list included `HTTP2`.

I verified the custom error page with:

```bash
curl -v --http2 -k -H "Accept: text/html" -i https://127.0.0.1:8443/missing
```

The output showed:

```text
ALPN: server accepted h2
using HTTP/2
HTTP/2 404
```

The response body contained the custom error page, including the marker, status `404`, and path `/missing`.

I verified the `/time` endpoint with:

```bash
curl -v --http2 -k -i https://127.0.0.1:8443/time
```

The output showed:

```text
ALPN: server accepted h2
using HTTP/2
HTTP/2 200
content-type: application/json
```

The response body contained a JSON object with the current server time, for example:

```json
{"time":"2026-09-30T18:01:04.4895067"}
```

I also inspected the generated certificate and confirmed that it contains:

```text
CN=localhost
DNS:localhost
IP Address:127.0.0.1
```

For the bonus, after each logical change I ran:

```bash
./gradlew.bat test ktlintCheck
./gradlew.bat check
```

The final commands completed with `BUILD SUCCESSFUL`. The automated tests cover the three content types, English and Spanish for each one, `406 Not Acceptable`, gzip on a real embedded server, and both conditional `304` cases with a fixed time provider.

I restarted the application with `./gradlew.bat bootRun` and used curl 8.22.0 with HTTP/2 support. For example, I checked compressed JSON with:

```bash
./curl.exe -vk --http2 --compressed -H "Accept: application/json" https://localhost:8443/time
```

The response included `ALPN: server accepted h2`, `using HTTP/2`, `Vary: accept-encoding`, and `Content-Encoding: gzip`.

I checked localized content with `Accept-Language: en` and `Accept-Language: es` for JSON, XML, and HTML. JSON and XML changed the `label` value while retaining the property names. The HTML response used the translated label and the matching `lang` attribute.

For conditional requests, I first saved the ETag and Last-Modified headers from a JSON response, then sent them in `If-None-Match` and `If-Modified-Since` requests before the minute changed. Both returned `HTTP/2 304` with no response body. When testing ETag in Windows PowerShell, I used a temporary curl header file so that the quotes in the weak ETag were preserved.

## AI disclosure

- **Tools / skills:** ChatGPT-5.6 Terra Medium.

- **Purpose:** I used AI assistance to inspect the lab requirements, explain the requested architecture, create small code and test skeletons, help with Kotlin/Ktlint formatting, generate and explain the local certificate commands, troubleshoot environment problems, review the Git workflow, and draft this report.

- **Representative prompts:** Examples include: `"Can you explain what this part of the error-page test does?"`, `"Why do we use a TimeProvider instead of calling LocalDateTime.now() directly?"`, `"How can I check that the /time endpoint is working correctly?"`, `"How can I verify that HTTP/2 has actually been negotiated?"`, and `"Can you review my REPORT.md?"`

- **Affected files/sections:** `build.gradle.kts`, `error.html`, `time.html`, `ErrorPageTest.kt`, `TimeComponent.kt`, `TimeController.kt`, `TimeViewConfiguration.kt`, `TimeControllerTest.kt`, `CompressionTest.kt`, message bundles, TLS and test YAML configuration files, certificate generation files, Git workflow/history for both the main implementation and the bonus, and this report.

- **Validation steps:** I reviewed each suggested change, ran `./gradlew.bat test ktlintCheck` and `./gradlew.bat check` throughout both the main implementation and the bonus, started the application manually, tested `/missing` and `/time`, and used curl 8.22.0 with HTTP/2 support to verify TLS/HTTP/2 negotiation, all /time representations and locales, gzip compression, and conditional `304` Not Modified responses.

- **Citations:** No external code snippets were directly adapted. The implementation follows the Lab 2 guide and the starter repository provided for the course.

- **Human-reviewed:** I reviewed the code and MVC configuration before keeping it, preserved `TimeProvider` as the source of time, separated the controller from the time component, checked the model shape, selected the snapshot semantics so HTTP validation remained truthful, reviewed the test strategy, added the English message bundle after detecting system-locale fallback in tests, added the representation/language tests, fixed the initial formatting issue, checked the generated certificate SANs and alias, diagnosed the occupied port and unsupported Windows curl client, and manually confirmed the final TLS, HTTP/2, Language, gzip, ETag, and Last-Modified behaviour.
