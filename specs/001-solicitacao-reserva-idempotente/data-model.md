# Data Model: Solicitacao de Reserva Idempotente

## Design Goal

O modelo inicial deve permitir que a aplicacao inicie com migrations versionadas e deve sustentar, sem remodelagem destrutiva, a futura decisao atomica de uma unica reserva confirmada por vaga e a repeticao do resultado por `requestId`.

## Entity: Vaga

Representa o recurso que um cliente tenta reservar.

| Field | Type | Rules | Purpose |
|-------|------|-------|---------|
| `id` | UUID | Primary key, required | Identificador recebido como `vagaId` |
| `status` | text enum | Required; `LIVRE` or `CONFIRMADA` nesta PoC | Estado observavel usado na confirmacao futura |
| `created_at` | timestamp with time zone | Required | Auditoria minima de criacao |
| `updated_at` | timestamp with time zone | Required | Auditoria minima de transicao |

## Entity: Reserva

Representa uma confirmacao produzida para uma vaga e um cliente.

| Field | Type | Rules | Purpose |
|-------|------|-------|---------|
| `id` | UUID | Primary key, required | Identificador devolvido como `reservaId` |
| `vaga_id` | UUID | Required, references `vaga.id` | Vaga confirmada |
| `cliente_id` | UUID | Required | Cliente beneficiario da confirmacao |
| `request_id` | UUID | Required, unique | Solicitacao que originou a reserva |
| `status` | text enum | Required; `CONFIRMADA` nesta fase | Resultado persistido da reserva |
| `confirmed_at` | timestamp with time zone | Required for `CONFIRMADA` | Momento da confirmacao |
| `created_at` | timestamp with time zone | Required | Auditoria minima |

### Reservation Constraints

- Somente uma `reserva` com `status = 'CONFIRMADA'` pode referenciar a mesma `vaga_id`.
- `request_id` nao pode originar mais de uma reserva.
- A aplicacao futura deve confirmar a vaga e gravar a reserva na mesma unidade transacional que conclui o resultado da requisicao.

## Entity: ResultadoRequisicao

Representa o primeiro resultado concluido para um `requestId` valido e fornece material suficiente para replay ou deteccao de uso inconsistente.

| Field | Type | Rules | Purpose |
|-------|------|-------|---------|
| `request_id` | UUID | Primary key, required | Chave idempotente fornecida pelo integrador |
| `vaga_id` | UUID | Required | Payload original para comparacao |
| `cliente_id` | UUID | Required | Payload original para comparacao |
| `resultado` | text enum | Required; `CONFIRMADA` or `CONFLITO_VAGA` inicialmente | Classe do resultado original reproduzivel |
| `http_status` | integer | Required; accepted values `201` or `409` para resultados persistidos | Status HTTP a repetir |
| `response_body` | json document | Required | Corpo exato ou dados deterministas da resposta original |
| `reserva_id` | UUID | Optional; references `reserva.id` | Presente somente em confirmacao |
| `created_at` | timestamp with time zone | Required | Momento em que o resultado foi concluido |

### Result Constraints

- Um `request_id` corresponde a exatamente um payload original e um resultado concluido.
- `resultado = 'CONFIRMADA'` requer `reserva_id` e `http_status = 201`.
- `resultado = 'CONFLITO_VAGA'` requer `reserva_id` ausente e `http_status = 409`.
- Erros de validacao nao geram resultado idempotente, pois nao constituem uma solicitacao valida identificavel.
- Reutilizacao inconsistente de um `requestId` existente e uma rejeicao da nova tentativa; ela nao substitui o resultado original persistido.

## Relationships

```text
Vaga 1 --- 0..1 Reserva(CONFIRMADA)
Reserva 1 --- 1 ResultadoRequisicao(CONFIRMADA)
ResultadoRequisicao(CONFLITO_VAGA) --- 0 Reserva
```

## Request Validation Rules

| Input field | Rule | Error outcome |
|-------------|------|---------------|
| `vagaId` | Required UUID | `400 VALIDATION_ERROR` |
| `clienteId` | Required UUID | `400 VALIDATION_ERROR` |
| `requestId` | Required UUID | `400 VALIDATION_ERROR` |
| Existing `requestId` with different `vagaId` or `clienteId` | Invalid reuse | `409 REQUEST_ID_INCONSISTENTE` |

## State Transitions

```text
Vaga: LIVRE -> CONFIRMADA

Nova solicitacao valida:
  vaga disponivel -> ResultadoRequisicao.CONFIRMADA + Reserva.CONFIRMADA
  vaga indisponivel -> ResultadoRequisicao.CONFLITO_VAGA

Repeticao consistente:
  ResultadoRequisicao existente -> devolve resultado original sem nova transicao

Repeticao inconsistente:
  ResultadoRequisicao existente + payload diferente -> rejeicao sem alterar estado
```

## Migration Direction

A migration inicial deve criar tipos/restricoes equivalentes para estados validos, chaves estrangeiras e unicidade de `request_id`, alem de um indice unico condicional para impedir duas reservas `CONFIRMADA` para a mesma vaga. A implementacao da logica que aciona essas garantias pode ser entregue em fase posterior; a estrutura nao deve depender de criacao automatica de schema em runtime.
