# Implementation Plan: Solicitacao de Reserva Idempotente

**Branch**: `001-solicitacao-reserva-idempotente` | **Date**: 2026-05-23 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-solicitacao-reserva-idempotente/spec.md` and technical direction for Phase 1 of the reservation PoC.

## Summary

Estabelecer a fundacao REST e persistente de `POST /reservas` em Java 21 com Spring Boot 3.5.14. A entrega implementa confirmacao, validacao e conflito para vaga ja confirmada; define o contrato futuro de `requestId` inconsistente e replay; modela vaga, reserva confirmada e resultado de requisicao; e prepara execucao com PostgreSQL e migrations Flyway. A migration inicial impede duas reservas confirmadas por vaga, enquanto o tratamento controlado de disputas simultaneas e a idempotencia completa serao implementados na fase posterior.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.5.14; Spring Web; Spring Validation; Spring Data JPA; Flyway (`flyway-core` e suporte PostgreSQL); PostgreSQL JDBC Driver

**Storage**: PostgreSQL como banco da aplicacao; migrations SQL versionadas pelo Flyway; H2 existente apenas para smoke tests que nao dependem de semantica especifica do PostgreSQL

**Testing**: JUnit 5 e Spring Boot Test; testes de contrato MVC para `POST /reservas`; testes de integracao com PostgreSQL conteinerizado para migrations e restricoes persistentes

**Target Platform**: Servico JVM executavel localmente e em container Linux, com PostgreSQL em ambiente local/conteinerizado

**Project Type**: Aplicacao REST backend unica com build Maven

**Performance Goals**: Pelo menos 95% de uma amostra de 100 solicitacoes individuais com resultado observavel em ate 2 segundos, conforme `SC-004`

**Constraints**: Resultado do endpoint deve ser distinguivel e estavel; PostgreSQL deve impedir desde a migration inicial mais de uma reserva confirmada por vaga; replay e conflito por `requestId` permanecem modelados sem processamento idempotente nesta fase; nenhuma dependencia de RabbitMQ ou Redis nesta fase

**Scale/Scope**: Uma operacao publica (`POST /reservas`), tres entidades persistentes principais e quatro classes de resultado; fundacao da PoC, sem cancelamento, consulta, eventos ou cache

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- A constituicao em `.specify/memory/constitution.md` ainda e um template nao ratificado, contendo placeholders e nenhuma regra executavel.
- Gate inicial: **PASS**. Nao ha principio definido que conflite com Java/Spring/PostgreSQL ou com a delimitacao desta fase.
- Restricao derivada da especificacao: o plano deve implementar confirmacao, validacao e conflito para vaga previamente confirmada, impedir duplicidade de confirmacao no modelo persistente e reservar idempotencia para a fase seguinte.
- Reavaliacao pos-design: **PASS**. O contrato define os quatro resultados; o modelo aplica a restricao de confirmacao unica nesta entrega e conserva estruturas necessarias para repeticao persistente futura.

## Project Structure

### Documentation (this feature)

```text
specs/001-solicitacao-reserva-idempotente/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- reservas.openapi.yaml
|-- checklists/
|   `-- requirements.md
`-- tasks.md                         # criado por /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
compose.yaml                         # PostgreSQL para execucao local/conteinerizada
Dockerfile                           # imagem executavel da aplicacao
src/main/java/com/mateuspaz/reservas/
|-- ReservasApplication.java
`-- reserva/
    |-- api/                         # controller, DTOs HTTP e tratamento de erros
    |-- application/                 # caso de uso de solicitacao
    `-- persistence/                 # entidades e repositorios JPA
src/main/resources/
|-- application.yaml
`-- db/migration/
    `-- V1__create_reservation_foundation.sql
src/test/java/com/mateuspaz/reservas/
|-- reserva/api/                     # testes do contrato HTTP
`-- reserva/persistence/             # migrations e restricoes PostgreSQL
src/test/resources/
`-- application-test.yaml
```

**Structure Decision**: Manter uma aplicacao Spring Boot unica e organizar a feature `reserva` por responsabilidade interna. A estrutura existente na raiz e em `src/` e ampliada sem criar novos modulos; `compose.yaml` e `Dockerfile` atendem a execucao exigida para a PoC.

## Phase 0: Research Decisions

O detalhamento e as alternativas avaliadas estao em [research.md](./research.md). As decisoes que guiam o desenho sao:

- manter a versao Spring Boot 3.5.14 ja declarada no projeto e Java 21;
- usar PostgreSQL como fonte de verdade e Flyway como unico mecanismo de evolucao do schema;
- definir contrato JSON sincronamente em `POST /reservas`, com `201`, `400` e `409`;
- preparar resultado por `requestId` para a fase de idempotencia e aplicar na migration inicial a restricao de confirmacao unica por vaga;
- usar PostgreSQL real em testes que validem migrations ou garantias dependentes do banco.

## Phase 1: Design & Contracts

- [data-model.md](./data-model.md) define campos, relacionamentos, validacoes e restricoes que suportam confirmacao unica e replay.
- [contracts/reservas.openapi.yaml](./contracts/reservas.openapi.yaml) estabiliza payloads e resultados HTTP de `POST /reservas`.
- [quickstart.md](./quickstart.md) descreve a execucao alvo com PostgreSQL, migrations e verificacao do contrato.
- `AGENTS.md` passa a apontar para este plano como contexto corrente para as tarefas seguintes.

## Implementation Sequence For Tasks

1. Ajustar dependencias e configuracao de ambientes para validacao, PostgreSQL, Flyway e testes PostgreSQL.
2. Incluir execucao conteinerizada minima (`compose.yaml` e `Dockerfile`) e configuracao local da aplicacao.
3. Criar a migration inicial para vaga, reserva e resultado de requisicao, incluindo restricoes desenhadas.
4. Criar DTOs e validacao de entrada conforme contrato e expor `POST /reservas`.
5. Implementar confirmacao, validacao e conflito para vaga previamente confirmada de forma aderente ao OpenAPI, sem tratar ainda replay ou reuso inconsistente de `requestId`.
6. Validar inicializacao, migrations, impedimento persistente de duas confirmacoes para uma vaga e contrato HTTP dos comportamentos executaveis desta entrega.

## Complexity Tracking

Nenhuma violacao constitucional foi identificada. O uso de PostgreSQL conteinerizado nos testes e necessario para validar migrations e restricoes especificas do banco adotado, sem adicionar arquitetura de producao alem do escopo.
