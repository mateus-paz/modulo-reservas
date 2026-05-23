# Quickstart: Solicitacao de Reserva Idempotente

## Purpose

Este guia descreve a execucao alvo da Fase 1: subir PostgreSQL, iniciar a aplicacao com migrations Flyway e inspecionar o contrato de `POST /reservas`. Os comandos tornam-se executaveis apos as tarefas de implementacao criarem os arquivos planejados.

## Prerequisites

- Java 21
- Docker com suporte a Compose
- Maven Wrapper presente no repositorio

## Start PostgreSQL

O plano inclui um `compose.yaml` com um servico `postgres` e banco `reservas`. Apos sua implementacao:

```bash
docker compose up -d postgres
```

Variaveis esperadas para a aplicacao local:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/reservas
export SPRING_DATASOURCE_USERNAME=reservas
export SPRING_DATASOURCE_PASSWORD=reservas
```

## Run The Application

```bash
./mvnw spring-boot:run
```

Ao inicializar, o Flyway deve aplicar as migrations de `src/main/resources/db/migration/`; falha de conexao ou migration invalida deve impedir a inicializacao bem-sucedida.

## Execute Automated Checks

```bash
./mvnw test
```

Os testes planejados devem cobrir:

- carregamento da aplicacao com configuracao de teste;
- aplicacao das migrations contra PostgreSQL;
- validade da restricao persistente que ja impede duplicidade de confirmacao;
- contrato HTTP de confirmacao, campos obrigatorios e conflito para vaga previamente confirmada.

## Inspect The HTTP Contract

O contrato fonte esta em `specs/001-solicitacao-reserva-idempotente/contracts/reservas.openapi.yaml`.

Exemplo de solicitacao valida:

```bash
curl -i -X POST http://localhost:8080/reservas \
  -H 'Content-Type: application/json' \
  -d '{"vagaId":"1d160ddf-a9bd-4c08-a7e3-75577d13db93","clienteId":"70d06015-f720-4bb8-a68b-9df34da75970","requestId":"995feb99-2517-462f-b212-09acdad109a8"}'
```

Exemplo de erro de validacao:

```bash
curl -i -X POST http://localhost:8080/reservas \
  -H 'Content-Type: application/json' \
  -d '{"vagaId":"nao-e-uuid","clienteId":null}'
```

## Expected Outcomes

| Situation | Status | Stable code |
|-----------|--------|-------------|
| Reserva confirmada | `201` | response status `CONFIRMADA` |
| Dados ausentes ou invalidos | `400` | `VALIDATION_ERROR` |
| Vaga previamente confirmada | `409` | `VAGA_EM_CONFLITO` |
| `requestId` reutilizado com outro payload (contrato futuro) | `409` | `REQUEST_ID_INCONSISTENTE` |

Nesta entrega, confirmacao, validacao e conflito para vaga previamente confirmada devem ser executaveis, e a migration deve impedir duplicidade de confirmacao. O tratamento controlado de disputa simultanea e o replay persistente sao alvo da fase seguinte.
