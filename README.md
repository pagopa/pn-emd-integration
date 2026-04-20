# pn-emd-integration

Microservizio reattivo Spring Boot (WebFlux) che fa da adattatore fra i servizi della piattaforma PagoPA Piattaforma Notifiche (`pn-*`) e le API della piattaforma EMD (Electronic Message Distribution).

Riceve le richieste dagli altri microservizi `pn-*` (principalmente `pn-delivery-push` e `pn-bff`) e le inoltra a EMD Core autenticandosi tramite token OAuth2 emessi da MIL Auth. Gestisce tre aree: invio di messaggi di cortesia (digitali e analogici) verso EMD, recupero e caching in Redis del `retrieval_payload` associato alla verifica di un TPP (Third Party Provider), generazione degli URL di pagamento per l'integrazione con le app bancarie.

Per le versioni delle dipendenze vedi `pom.xml`.


## Architettura

```
PnEmdIntegrationController
  └─ EmdCoreServiceImpl          (orchestratore: delega tutto, nessuna logica propria)
       ├─ EmdMessageServiceImpl   → EmdClientImpl → EMD Core (message)
       ├─ EmdRetrievalServiceImpl → EmdClientImpl + RetrievalPayloadRedisService (cache-aside)
       └─ EmdPaymentServiceImpl   → EmdClientImpl → EMD Core (payment)

Token chain:
AccessTokenExpiringMap → MilAuthClient → MilAuthClientImpl → MIL Auth API
```

Il controller implementa le interfacce generate da OpenAPI (`MessageApi`, `PaymentApi`, `CheckTppApi`). `EmdCoreServiceImpl` è un puro delegatore; la logica di dominio vive nei service `Emd*ServiceImpl`. Ogni service di dominio ha una variante `*Disabled` selezionata via `@ConditionalOnProperty` per il feature toggle. `AccessTokenExpiringMap` (basato su `net.jodah.ExpiringMap`) mantiene in memoria il token MIL con refresh anticipato rispetto alla scadenza effettiva.


## API e documentazione

Le specifiche OpenAPI degli endpoint esposti sono in [docs/openapi/api-private.yaml](docs/openapi/api-private.yaml). I client per MIL Auth ed EMD Core sono generati in fase di build da spec remote referenziate nel `pom.xml`.

| Metodo | Path | Metodo controller | Sequence diagram |
|--------|------|-------------------|------------------|
| POST | `/emd-integration-private/send-message` | `sendMessage` | [InvioMessaggioCortesia](docs/sequences/InvioMessaggioCortesia.md) |
| GET | `/emd-integration-private/payment-url` | `getPaymentUrl` | [PagamentoTramiteAppBanca](docs/sequences/PagamentoTramiteAppBanca.md) |
| GET | `/emd-integration-private/emd/check-tpp` | `emdCheckTPP` | [AccessoAlDettaglioDellaNotifica](docs/sequences/AccessoAlDettaglioDellaNotifica.md) |
| GET | `/emd-integration-private/token/check-tpp` | `tokenCheckTPP` | [AccessoAlDettaglioDellaNotifica](docs/sequences/AccessoAlDettaglioDellaNotifica.md) |

I due endpoint `check-tpp` hanno comportamenti diversi: `emdCheckTPP` chiama sempre EMD Core e restituisce un payload fresco; `tokenCheckTPP` applica il pattern cache-aside su Redis (miss → EMD Core + scrittura in cache) ed è pensato per essere invocato dal BFF a ogni richiesta utente.


## Configurazione

Le property vanno definite in `config/application.properties` (formato Spring Boot, dotted-lowercase). Spring Boot supporta il relaxed binding: per passarle come env var, converti punti e trattini in underscore e metti tutto in maiuscolo. Esempio: `pn.emd-integration.mil-base-path` → `PN_EMDINTEGRATION_MILBASEPATH`.

### Feature toggle

| Property | Tipo | Default | Esempio |
|----------|------|---------|---------|
| `pn.emd-integration.enabled` | boolean | `true` | `true` |
| `pn.emd-integration.message.enabled` | boolean | `true` | `true` |
| `pn.emd-integration.retrieval.enabled` | boolean | `true` | `true` |
| `pn.emd-integration.payment.enabled` | boolean | `true` | `true` |
| `pn.emd-integration.enable-api-v2` | boolean | `true` | `true` |

### Autenticazione MIL Auth

| Property | Tipo | Default | Esempio |
|----------|------|---------|---------|
| `pn.emd-integration.mil-client-id` | String | — | `<client-id>` |
| `pn.emd-integration.mil-client-secret` | String | — | `<client-secret>` |
| `pn.emd-integration.mil-base-path` | String (URL) | — | `https://api-mcshared.<env>.cstar.pagopa.it/auth` |
| `pn.emd-integration.mil-token-expiration-buffer` | long (ms) | — | `30000` |

### EMD Core

| Property | Tipo | Default | Esempio |
|----------|------|---------|---------|
| `pn.emd-integration.emd-core-message-base-path` | String (URL) | — | `https://emdapi.<env>.cstar.pagopa.it/message-core` |
| `pn.emd-integration.emd-core-payment-base-path` | String (URL) | — | `https://emdapi.<env>.cstar.pagopa.it/payment` |
| `pn.emd-integration.original-message-url` | String (URL) | — | `https://cittadini.<env>.pagopa.it` |
| `pn.emd-integration.emd-payment-endpoint` | String (URL) | — | `https://<tpp-endpoint>` |
| `pn.emd-integration.courtesy-message-content` | String | — | `Hai ricevuto una notifica!` |

### Cache Redis

| Property | Tipo | Default | Esempio |
|----------|------|---------|---------|
| `pn.emd-integration.redis-cache.host-name` | String | `localhost` | `<elasticache-endpoint>` |
| `pn.emd-integration.redis-cache.port` | int | `6379` | `6379` |
| `pn.emd-integration.redis-cache.user-id` | String | — | `<iam-user>` |
| `pn.emd-integration.redis-cache.cache-name` | String | — | `<cache-name>` |
| `pn.emd-integration.redis-cache.cache-region` | String | — | `eu-south-1` |
| `pn.emd-integration.redis-cache.mode` | enum | — | `SERVERLESS` o `MANAGED` |
| `pn.emd-integration.retrieval-payload-cache-ttl` | Duration | `PT10M` | `PT10M` |

La modalità `SERVERLESS` abilita TLS e aggiunge il parametro `ResourceType=ServerlessCache` all'URL pre-signed usato come password IAM; la modalità `MANAGED` usa il connection pool Jedis. In profilo `local` (avvio con `docker compose up`) Redis è usato senza SSL e senza autenticazione.

### Altro

| Property | Tipo | Default | Esempio |
|----------|------|---------|---------|
| `cors.allowed.domains` | String (CSV) | — | `http://localhost:8090,http://localhost:8091` |
| `aws.region-code` | String | — | `eu-south-1` |
| `aws.endpoint-url` | String (URL) | — | `http://localhost:4566` |


## Esecuzione

### Prerequisiti

Prima di avviare l'applicazione in locale è necessario far partire l'istanza Redis dichiarata in `docker-compose.yml`.

```bash
docker compose up
```

### Compilazione

```bash
./mvnw clean install
```

Il build esegue anche la code generation OpenAPI (server stub da `docs/openapi/api-private.yaml`, client MIL Auth ed EMD Core da spec remote definite in `pom.xml`). I package generati sono sotto `it.pagopa.pn.emdintegration.generated.openapi.*` e non devono essere modificati a mano.

### Avvio locale

```bash
./mvnw spring-boot:run
```

Gli override delle property locali vanno in `config/application.properties` (profilo `local` attivo di default). L'applicazione sarà disponibile su `http://localhost:8080`.

### Test

```bash
./mvnw test
```

Per eseguire una singola classe o metodo di test:

```bash
./mvnw test -Dtest=EmdMessageServiceImplTest
./mvnw test -Dtest=EmdMessageServiceImplTest#testSubmitMessageAnalog
```

I test sono unitari (nessun `@SpringBootTest`), con dipendenze mockate via Mockito e pipeline reattive verificate con `reactor.test.StepVerifier`.
