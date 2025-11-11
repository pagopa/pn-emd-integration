```mermaid
sequenceDiagram
	%% Attori principali
	participant Delivery Push
	participant Emd Integration
	participant Emd Core

	%% Invio messaggio di cortesia (send-message API)
	Delivery Push->>Emd Integration: POST /emd-integration-private/send-message (SendMessageRequestBody)
	Note right of Delivery Push: Richiesta invio messaggio cortesia (sendMessage)
	Emd Integration->>Emd Integration: Validazione richiesta
	alt Richiesta non valida
		Emd Integration-->>Delivery Push: 400 Problem
	else Richiesta valida
		Note over Emd Integration,Emd Core: Invocazione servizio Core
		Emd Integration->>Emd Core: POST /message-core/sendMessage (SendMessageRequest)
		Note right of Emd Integration: Headers: Authorization: Bearer <JWT> | RequestId: <UUID>
		Note over Emd Integration,Emd Core: Payload JSON ->\n\tmessageId\n\trecipientId\n\ttriggerDateTime\n\tsenderDescription\n\tmessageUrl\n\toriginId\n\tcontent\n\tassociatedPayment\n\tchannel=SEND
		alt Core: NO_CHANNELS_ENABLED (200)
			Emd Core-->>Emd Integration: 200 outcome=NO_CHANNELS_ENABLED
		else Core: OK (202)
			Emd Core-->>Emd Integration: 202 outcome=OK
		else Core error
			Emd Core-->>Emd Integration: 4xx/5xx Error
		end
		alt Esito NO_CHANNELS_ENABLED | OK
			Emd Integration-->>Delivery Push: 200 SendMessageResponse(outcome)
		else Errore da Core
			Emd Integration-->>Delivery Push: 500 Problem (mapping errore)
		end
	end
```

#### Esempio `sendMessageRequest`
```json
{
	"messageId": "8a32fa8a-5036-4b39-8f2e-47d3a6d23f9e",
	"recipientId": "RSSMRA85T10A562S",
	"triggerDateTime": "2024-06-21T12:34:56.000Z",
	"senderDescription": "Comune di Pontecagnano",
	"messageUrl": "http://wwww.google.it",
	"originId": "XRUZ-GZAJ-ZUEJ-202407-W-1",
	"title": "Hai una comunicazione a valore legale su SEND",
	"content": "Ciao,\nhai ricevuto una notifica SEND, cioè una comunicazione a valore legale emessa da un’amministrazione.\n\nPer leggerla e conoscere tutti i dettagli, accedi al sito web di SEND direttamente da questo messaggio entro il 2024-06-21 alle 12:34 eviterai una raccomandata cartacea e i relativi costi.",
	"analogSchedulingDate": "2024-06-26T12:34:56.000Z",
	"workflowType": "ANALOG",
	"associatedPayment": true,
	"channel": "SEND"
}
```
