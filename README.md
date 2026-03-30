# pn-emd-integration

## Indice del README
- [Descrizione](#descrizione)
- [Tecnologie Utilizzate](#tecnologie-utilizzate)
- [Architettura](#architettura)
- [API & Documentazione](#api--documentazione)
- [Configurazione](#configurazione)
- [Esecuzione](#esecuzione)

## Descrizione

Microservizio che si integra con le API esposte dai servizi EMD (Electronic Message Distribution) per fornire la capacità di inviare messaggi via canali terzi (digitali e analogici). Il servizio funge da adattatore tra il sistema PagoPA e la piattaforma EMD, gestendo:

- **Invio messaggi**: sottomissione di messaggi multicanale (digitale/analogico) verso EMD
- **Recupero payload**: gestione e caching dei dati di recupero per i TPP (Third Party Provider)
- **Integrazione pagamenti**: generazione di URL di pagamento per i servizi di pagamento
- **Autenticazione IAM**: gestione di token per l'accesso alle API downstream

## Tecnologie Utilizzate

### Stack Principale
- **Java 11** + **Spring Boot 2.x** (parent: `pn-parent:2.1.1`)
- **Spring WebFlux** (reactive)
- **Project Reactor** (Mono/Flux)
- **OpenAPI 3.0.3** (code generation con `openapi-generator-maven-plugin`)

### Storage e Infrastruttura
- **Redis** (cache, ElastiCache in prod con autenticazione IAM)
- **AWS SDK v1** (per IAM auth con ElastiCache)
- **Jedis** (client Redis)

### Dipendenze Interne
- `pn-commons:2.10.0` (commons PagoPA)
- Endpoint di **MIL Auth** (`mil-auth-client.yaml`): OAuth2 client credentials per ottenere token di accesso
- Endpoint di **EMD Core** (`emd-core-client.yaml`): API principale per invio messaggi e recupero payload

### Dipendenze Esterne
- **MIL Auth Service**: REST via OAuth2 client credentials (`POST /token`)
- **EMD Core API**: REST per message submission e retrieval (`POST /message-core/sendMessage`, `GET /payment/retrievalTokens/{retrievalId}`)

## Architettura

```
┌─────────────────────────────────────────────────────────────────┐
│                     PnEmdIntegrationController                  │
│         (implements MessageApi, PaymentApi, CheckTppApi)        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                     EmdCoreServiceImpl                           │
│                   (orchestrator, delegates)                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼────────┐  ┌──────▼────────┐  ┌────▼──────────┐
│ EmdMessageSvc  │  │EmdRetrievalSvc│  │EmdPaymentSvc  │
│ (send message) │  │(cache-aside)  │  │(payment urls) │
└───────┬────────┘  └──────┬────────┘  └────┬──────────┘
        │                  │                 │
        └──────────────────┼─────────────────┘
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
┌───────▼────────────────┐      ┌────────────▼────────┐
│   EmdClientImpl         │      │ AccessTokenExpiring │
│ (HTTP calls to EMD)    │      │ Map (token cache)   │
└───────┬────────────────┘      └────────────┬────────┘
        │                                    │
        │                        ┌───────────▼────────────┐
        │                        │  MilAuthClientImpl      │
        │                        │  (get auth token)      │
        │                        └───────────┬────────────┘
        │                                    │
        │                   ┌────────────────┼──────────────┐
        └───────────────────┤                              │
                            │                              │
                    ┌───────▼───────┐         ┌────────────▼──────┐
                    │  MIL Auth API │         │ EMD Core API      │
                    │  (OAuth2)     │         │ (multi-channel)   │
                    └───────────────┘         └───────────────────┘
```

**Storage**: Redis (RetrievalPayloadRedisService per cache-aside pattern, TTL 10m)

## API & Documentazione

### Swagger/OpenAPI
- **API Server**: `docs/openapi/api-private.yaml` — endpoint privati (MessageApi, PaymentApi, CheckTppApi)
- **Client EMD Core**: `docs/wsclient/emd-core-client.yaml` — client generato per EMD Core
- **Client MIL Auth**: `docs/wsclient/mil-auth-client.yaml` — client generato per token OAuth2

### Sequenze e Casi d'Uso
- `docs/sequences/InvioMessaggioCortesia.md` — Flusso di invio messaggi
- `docs/sequences/AccessoAlDettaglioDellaNotifica.md` — Accesso ai dettagli
- `docs/sequences/PagamentoTramiteAppBanca.md` — Flusso pagamenti

## Configurazione

### Variabili d'Ambiente Principali
```
# Integrazione abilitata
PN_EMDINTEGRATION_ISINTEGRATIONENABLED=true

# Servizi di dominio abilitati (feature flags)
PN.EMD-INTEGRATION.MESSAGE.ENABLED=true
PN.EMD-INTEGRATION.RETRIEVAL.ENABLED=true
PN.EMD-INTEGRATION.PAYMENT.ENABLED=true

# MIL Auth
PN.EMD-INTEGRATION.MIL-CLIENT-ID=<client-id>
PN.EMD-INTEGRATION.MIL-CLIENT-SECRET=<client-secret>
PN.EMD-INTEGRATION.MIL-BASE-PATH=https://api-mcshared.{env}.cstar.pagopa.it/auth

# EMD Core
PN.EMD-INTEGRATION.EMD-CORE-BASE-PATH=https://emdapi.{env}.cstar.pagopa.it

# Redis (non-local)
PN.EMD-INTEGRATION.REDIS-CACHE.HOST-NAME=<elasticache-endpoint>
PN.EMD-INTEGRATION.REDIS-CACHE.PORT=6379
PN.EMD-INTEGRATION.REDIS-CACHE.USER-ID=<iam-user>
PN.EMD-INTEGRATION.REDIS-CACHE.CACHE-NAME=<cache-name>
PN.EMD-INTEGRATION.REDIS-CACHE.CACHE-REGION=<aws-region>
PN.EMD-INTEGRATION.REDIS-CACHE.MODE=SERVERLESS (o MANAGED)

# Cache TTL
PN.EMD-INTEGRATION.RETRIEVAL-PAYLOAD-CACHE-TTL=PT10M

# CORS
CORS.ALLOWED.DOMAINS=http://localhost:8090,http://localhost:8091
```

## Esecuzione

### Compilazione
```bash
./mvnw clean install
```

### Avvio locale

1. **Avviare Redis**
```bash
docker compose up
```

2. **Avviare l'applicazione**
```bash
./mvnw spring-boot:run
```

L'applicazione sarà disponibile su `http://localhost:8080`.

### Test

```bash
# Eseguire tutti i test
./mvnw test

# Eseguire un test specifico
./mvnw test -Dtest=EmdMessageServiceImplTest

# Eseguire un singolo metodo di test
./mvnw test -Dtest=EmdMessageServiceImplTest#testSubmitMessageAnalog
```
