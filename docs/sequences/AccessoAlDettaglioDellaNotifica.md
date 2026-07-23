### 1A. Autenticazione (login SPID e token_send)
```mermaid
sequenceDiagram
    participant App Banca
    participant SEND FE
    participant SpidHub
    participant TokenExchange

    App Banca->>SEND FE: GET /cittadini?retrieval_id=test
    Note over SEND FE: retrieval_id salvato in sessione browser
    SEND FE->>SEND FE: Check login

    alt Utente NON loggato
        SEND FE->>App Banca: Redirect /cittadini/auth/login?retrieval_id=test
        App Banca->>SEND FE: GET /cittadini/auth/login?retrieval_id=test
        SEND FE->>SpidHub: Richiesta autenticazione
        SpidHub-->>SEND FE: token_spidHub
        SEND FE->>App Banca: Redirect /cittadini?retrieval_id=test&token=token_spidHub
        Note over SEND FE: recupero retrieval_id dalla sessione
        SEND FE->>TokenExchange: Richiesta token_send (token_spidHub, retrieval_id)
        TokenExchange-->>SEND FE: token_send
        Note over TokenExchange: token_send include retrieval_id + tpp_id
        Note over SEND FE: Salva token_send (sessionStorage)
    else Utente già loggato
        SEND FE->>TokenExchange: Richiesta token_send
        TokenExchange-->>SEND FE: token_send
    end
```

### 1B. Autorizzazione e recupero retrieval_payload (verifica TPP)
```mermaid
sequenceDiagram
    participant SEND FE
    participant Bff
    participant Emd Integration
    participant Emd Core

    SEND FE->>Bff: GET /bff/v1/notifications/received/check-tpp (retrieval_id)
    Note over SEND FE,Bff: Authorization: Bearer token_send
    alt Utente loggato tramite ALTRO flusso
        Bff-->>SEND FE: Errore: utente non proviene da TPP
    else Utente loggato tramite login di sopra
        Bff->>Emd Integration: GET /emd/checkTPP (retrieval_id)
        Emd Integration->>Emd Core: GET /payment/retrievalTokens/{retrievalId}
        Emd Core-->>Emd Integration: retrieval_payload
        Emd Integration-->>Bff: retrieval_payload
        Bff-->>SEND FE: retrieval_payload
    end
```

#### Esempio `retrieval_payload`
```json
{
    "retrievalId": "0e4c6629-8753-234s-b0da-1f796999ec2-15038637960920",
    "tppId": "0e3bee29-8753-447c-b0da-1f7965558ec2-1706867960900",
    "deeplink": "https://example.com/deeplink/123e4567-e89b-12d3-a456-426614174000?userId=1234567890&session=abcdef",
    "pspDenomination": "Banca1",
    "originId": "XRUZ-GZAJ-ZUEJ-202407-W-1",
    "isPaymentEnabled": true
}
```

### 2. Accesso al dettaglio notifica (IUN)
```mermaid
sequenceDiagram
    participant SEND FE
    participant Bff
    participant Delivery

    SEND FE->>SEND FE: Navigazione /cittadini/notifiche/IUN/dettaglio
    SEND FE->>Bff: GET /bff/v1/notifications/received/IUN
    Bff->>Delivery: GET /delivery/v2.x/notifications/received/IUN
    Delivery-->>Bff: notifica
    Bff-->>SEND FE: notifica
```

