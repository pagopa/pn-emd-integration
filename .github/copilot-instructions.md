# Copilot Instructions for pn-emd-integration

## Project Overview

Reactive Spring Boot microservice (WebFlux) that integrates with EMD (Electronic Message Distribution) services to send notifications via digital and analog channels on PagoPA's platform. It handles three domains: message submission, retrieval payload caching, and payment URL generation.

## Build & Run

```bash
./mvnw clean install          # Full build (also triggers OpenAPI code generation)
./mvnw test                   # All tests
./mvnw spring-boot:run        # Run locally (requires Redis — see below)

docker compose up             # Start local Redis before running the app
```

**Single test:**
```bash
./mvnw test -Dtest=EmdMessageServiceImplTest
./mvnw test -Dtest=EmdMessageServiceImplTest#testSubmitMessageAnalog
```

## Architecture

```
PnEmdIntegrationController
  └─ EmdCoreServiceImpl          (orchestrator — delegates only)
       ├─ EmdMessageServiceImpl  → EmdClientImpl → EMD Core API
       ├─ EmdRetrievalServiceImpl → EmdClientImpl + ReactiveRedisService (cache-aside)
       └─ EmdPaymentServiceImpl  → EmdClientImpl
            ↑
       All clients need a Bearer token: AccessTokenExpiringMap → MilAuthClientImpl → MIL Auth API
```

The controller implements OpenAPI-generated interfaces (`MessageApi`, `PaymentApi`, `CheckTppApi`). The core service is a pure delegator; business logic lives in domain services.

## OpenAPI Code Generation

Code is generated from OpenAPI specs during the `generate-resources` Maven phase. **Never edit generated classes directly.**

| Execution | Input spec | Output package |
|---|---|---|
| Server stubs | `docs/openapi/api-private.yaml` | `it.pagopa.pn.emdintegration.generated.openapi.server.v1` |
| MIL Auth client | `docs/wsclient/mil-auth-client.yaml` | `it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth` |
| EMD Core client | `docs/wsclient/emd-core-client.yaml` | `it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient` |

When adding or modifying API endpoints: edit the relevant `.yaml` in `docs/`, then rebuild to regenerate. The server uses the **delegate pattern** with `interfaceOnly=true` and `reactive=true`.

## Feature Toggle Pattern

Each domain service has an `*Impl` and a `*Disabled` variant, selected via `@ConditionalOnProperty`:

```java
// Enabled (default)
@ConditionalOnProperty(name = "pn.emd-integration.message.enabled", havingValue = "true", matchIfMissing = true)
public class EmdMessageServiceImpl implements EmdMessageService { ... }

// Disabled — returns a safe no-op response
@ConditionalOnProperty(name = "pn.emd-integration.message.enabled", havingValue = "false")
public class EmdMessageServiceDisabled implements EmdMessageService { ... }
```

Properties controlling this: `pn.emd-integration.message.enabled`, `pn.emd-integration.retrieval.enabled`, `pn.emd-integration.payment.enabled`.

## Key Conventions

- **`*Impl`** for service/client implementations; **`*Disabled`** for no-op feature-toggle variants.
- Controllers implement generated interfaces and use `@RequiredArgsConstructor` + `@Slf4j`.
- All async operations return `Mono<T>` — no blocking code.
- Config properties are bound via `@ConfigurationProperties(prefix = "pn.emd-integration")` on `PnEmdIntegrationConfigs`. Add new properties there, not as loose `@Value` fields.
- Exception codes live in `PnEmdIntegrationExceptionCodes`; throw `PnEmdIntegrationException` or `PnEmdIntegrationNotFoundException`.
- Constants go in `PnEmdIntegrationCostants` (note: intentional typo in class name — keep it consistent).
- Lombok annotations in use: `@Data`, `@RequiredArgsConstructor`, `@Slf4j`, `@CustomLog` (PagoPA variant), `@Builder(toBuilder = true)`.

## Token Caching

`AccessTokenExpiringMap` wraps `net.jodah.ExpiringMap` to cache the MIL Bearer token in memory with variable TTL. It also respects a `pn.emd-integration.mil-token-expiration-buffer` to refresh the token before it actually expires. This is injected into every service that calls an external API.

## Redis Cache

`ReactiveRedisService<T>` wraps Spring Data Redis reactively. The only concrete implementation is `RetrievalPayloadRedisService`, used by `EmdRetrievalServiceImpl` for cache-aside on `RetrievalPayload`. TTL is configured via `pn.emd-integration.retrieval-payload-cache-ttl` (default: `PT10M`). Cache keys follow the pattern `pn-emd::retrievalPayload::<retrievalId>`.

**Redis errors are intentionally swallowed** — `get`/`set`/`delete` all `onErrorResume` to `Mono.empty()` and log a warning. The cache is best-effort; a miss always falls through to the upstream EMD client.

### ElastiCache IAM Authentication (`@Profile("!local")`)

`CacheConfig` has two beans split by Spring profile:
- `@Profile("local")` — plain Jedis, no SSL, no auth (used with `docker compose up`)
- `@Profile("!local")` — `PnEmdIntegrationConnectionFactory` (extends `JedisConnectionFactory`) with IAM auth

For non-local environments (AWS ElastiCache), authentication uses a **pre-signed URL as the Redis password** (AWS Sig4, valid 15 min). `IAMAuthTokenRequest` generates this URL; `PnEmdIntegrationConnectionFactory` schedules a token refresh every 14 minutes via a `ScheduledExecutorService`, updating the password and calling `afterPropertiesSet()` to apply it. `RedisInitializer` triggers the first auth token on `ApplicationReadyEvent`.

Redis mode (`pn.emd-integration.redis-cache.mode`): `SERVERLESS` adds the `ResourceType=ServerlessCache` parameter to the presigned URL and enables TLS; `MANAGED` uses Jedis connection pooling instead.

## Message Templates

Markdown templates for digital/analog courtesy messages live in `src/main/resources/message_templates/`. They are loaded at startup by `PnEmdIntegrationConfigs#init()` and cached in memory. Analog templates support `{{date}}` and `{{time}}` placeholders, which are filled at request time using the `Europe/Rome` timezone.

## Testing Patterns

- Unit tests only — no `@SpringBootTest`; all dependencies mocked via Mockito.
- Initialize mocks with `MockitoAnnotations.openMocks(this)` in `@BeforeEach`.
- Verify reactive pipelines with `reactor.test.StepVerifier.create(...)`.
- Test templates are in `src/test/resources/message_templates/`.
- Disabled service tests verify the no-op behaviour of `*Disabled` classes.

## Local Configuration

Override defaults in `config/application.properties` for local development:
- AWS LocalStack: `aws.endpoint-url=http://localhost:4566`
- Redis: `pn.emd-integration.redis-cache.host-name=localhost`
- EMD mock: `pn.emd-integration.emd-core-base-path=http://localhost:1080/emd-mock`
