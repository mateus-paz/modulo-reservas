# Tasks: Solicitacao de Reserva Idempotente

**Input**: Design documents from `specs/001-solicitacao-reserva-idempotente/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/reservas.openapi.yaml`, `quickstart.md`

**Tests**: Incluidos porque os criterios `SC-002` e `SC-006` exigem verificacao automatizada do contrato executavel e da restricao persistente.

**Organization**: As tarefas sao agrupadas por historia; replay idempotente, rejeicao executavel de `REQUEST_ID_INCONSISTENTE` e tratamento de disputa simultanea permanecem fora desta entrega.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode ser executada em paralelo com outras tarefas marcadas na mesma fase quando os arquivos nao se sobrepoem.
- **[Story]**: Identifica a historia atendida (`US1`, `US2`, `US3`).
- Todos os itens abaixo indicam arquivos concretos da implementacao.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar dependencias e execucao local/conteinerizada para PostgreSQL e testes.

- [ ] T001 Atualizar dependencias de validacao e testes PostgreSQL/Testcontainers em `pom.xml`
- [ ] T002 [P] Configurar datasource PostgreSQL, JPA e Flyway para execucao local em `src/main/resources/application.yaml`
- [ ] T003 [P] Definir o servico PostgreSQL local e variaveis da aplicacao em `compose.yaml`
- [ ] T004 [P] Criar a imagem executavel Spring Boot para uso conteinerizado em `Dockerfile`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Criar a base persistente e de teste exigida por todas as historias.

**CRITICAL**: Nenhuma historia pode ser entregue antes da migration e das entidades compartilhadas estarem disponiveis.

- [ ] T005 Criar a migration de `vaga`, `reserva` e `resultado_requisicao`, incluindo estados, chaves e indice unico de reserva confirmada por vaga, em `src/main/resources/db/migration/V1__create_reservation_foundation.sql`
- [ ] T006 [P] Criar enums persistentes de estado e resultado em `src/main/java/com/mateuspaz/reservas/reserva/persistence/VagaStatus.java`, `src/main/java/com/mateuspaz/reservas/reserva/persistence/ReservaStatus.java` e `src/main/java/com/mateuspaz/reservas/reserva/persistence/ResultadoSolicitacao.java`
- [ ] T007 Criar entidades JPA mapeadas para a migration em `src/main/java/com/mateuspaz/reservas/reserva/persistence/VagaEntity.java`, `src/main/java/com/mateuspaz/reservas/reserva/persistence/ReservaEntity.java` e `src/main/java/com/mateuspaz/reservas/reserva/persistence/ResultadoRequisicaoEntity.java`
- [ ] T008 Criar repositorios JPA para as tres entidades em `src/main/java/com/mateuspaz/reservas/reserva/persistence/VagaRepository.java`, `src/main/java/com/mateuspaz/reservas/reserva/persistence/ReservaRepository.java` e `src/main/java/com/mateuspaz/reservas/reserva/persistence/ResultadoRequisicaoRepository.java`
- [ ] T009 [P] Configurar perfil de integracao PostgreSQL e base Testcontainers em `src/test/resources/application-integration.yaml` e `src/test/java/com/mateuspaz/reservas/support/PostgresIntegrationTest.java`
- [ ] T010 Validar inicializacao da aplicacao com migrations aplicadas em PostgreSQL em `src/test/java/com/mateuspaz/reservas/ReservasPostgresApplicationTests.java`

**Checkpoint**: Banco, migrations, mapeamentos e ambiente de integracao prontos para as historias.

---

## Phase 3: User Story 1 - Solicitar uma reserva valida (Priority: P1) MVP

**Goal**: Confirmar uma solicitacao valida para vaga disponivel e devolver identificadores no resultado.

**Independent Test**: Criar uma vaga livre, executar `POST /reservas` com UUIDs validos e verificar resposta `201 CONFIRMADA` vinculada a `vagaId`, `clienteId` e `requestId`.

### Tests for User Story 1

- [ ] T011 [P] [US1] Criar teste de contrato para resposta `201 CONFIRMADA` de `POST /reservas` em `src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaSuccessContractTest.java`
- [ ] T012 [P] [US1] Criar teste de integracao da confirmacao e persistencia da reserva/resultado em `src/test/java/com/mateuspaz/reservas/reserva/application/ConfirmarReservaIntegrationTest.java`

### Implementation for User Story 1

- [ ] T013 [P] [US1] Criar DTOs de requisicao e confirmacao conforme OpenAPI em `src/main/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaRequest.java` e `src/main/java/com/mateuspaz/reservas/reserva/api/ReservaConfirmadaResponse.java`
- [ ] T014 [US1] Implementar o caso de uso que confirma vaga livre e persiste reserva e resultado `CONFIRMADA` em `src/main/java/com/mateuspaz/reservas/reserva/application/SolicitacaoReservaService.java`
- [ ] T015 [US1] Expor `POST /reservas` e mapear resposta de confirmacao em `src/main/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaController.java`

**Checkpoint**: A solicitacao valida e confirmada e testavel isoladamente como MVP.

---

## Phase 4: User Story 2 - Rejeitar solicitacao invalida (Priority: P2)

**Goal**: Retornar erro de validacao legivel quando `vagaId`, `clienteId` ou `requestId` estiver ausente ou invalido.

**Independent Test**: Enviar payloads com cada campo ausente ou fora do formato UUID e verificar `400 VALIDATION_ERROR` com os campos a corrigir, sem reserva persistida.

### Tests for User Story 2

- [ ] T016 [P] [US2] Criar testes de contrato para campos obrigatorios e formato UUID invalido em `src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaValidationContractTest.java`

### Implementation for User Story 2

- [ ] T017 [US2] Aplicar validacoes de entrada do endpoint em `src/main/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaRequest.java` e `src/main/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaController.java`
- [ ] T018 [P] [US2] Criar modelos HTTP de erro de validacao em `src/main/java/com/mateuspaz/reservas/reserva/api/ApiErrorDetail.java` e `src/main/java/com/mateuspaz/reservas/reserva/api/ValidationErrorResponse.java`
- [ ] T019 [US2] Mapear falhas de validacao para `400 VALIDATION_ERROR` em `src/main/java/com/mateuspaz/reservas/reserva/api/ReservaExceptionHandler.java`

**Checkpoint**: Entradas invalidas sao rejeitadas independentemente do fluxo de conflito.

---

## Phase 5: User Story 3 - Rejeitar vaga ja indisponivel (Priority: P3)

**Goal**: Rejeitar uma solicitacao valida para vaga previamente confirmada e comprovar a protecao persistente contra duplicidade.

**Independent Test**: Persistir previamente uma confirmacao para a vaga, enviar nova solicitacao sem simultaneidade e verificar `409 VAGA_EM_CONFLITO`; tentar inserir uma segunda confirmacao diretamente e verificar que a migration impede a duplicidade.

### Tests for User Story 3

- [ ] T020 [P] [US3] Criar teste de contrato para resposta `409 VAGA_EM_CONFLITO` em `src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaConflictContractTest.java`
- [ ] T021 [P] [US3] Criar teste PostgreSQL para o indice unico de reserva confirmada por vaga em `src/test/java/com/mateuspaz/reservas/reserva/persistence/ReservaConfirmationConstraintIntegrationTest.java`
- [ ] T022 [P] [US3] Criar teste de integracao para conflito sequencial sem nova reserva em `src/test/java/com/mateuspaz/reservas/reserva/application/RejeitarVagaConfirmadaIntegrationTest.java`

### Implementation for User Story 3

- [ ] T023 [P] [US3] Criar resposta e excecao de conflito de vaga em `src/main/java/com/mateuspaz/reservas/reserva/api/ConflictErrorResponse.java` e `src/main/java/com/mateuspaz/reservas/reserva/application/VagaEmConflitoException.java`
- [ ] T024 [US3] Implementar verificacao de vaga previamente confirmada e persistencia do resultado `CONFLITO_VAGA` em `src/main/java/com/mateuspaz/reservas/reserva/application/SolicitacaoReservaService.java`
- [ ] T025 [US3] Mapear conflito conhecido para `409 VAGA_EM_CONFLITO` sem tratar corridas simultaneas ou `REQUEST_ID_INCONSISTENTE` em `src/main/java/com/mateuspaz/reservas/reserva/api/ReservaExceptionHandler.java`

**Checkpoint**: Os tres resultados executaveis desta entrega estao implementados e a protecao persistente foi comprovada.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalizar verificacao operacional e manter a documentacao aderente ao comportamento entregue.

- [ ] T026 [P] Revisar exemplos de execucao, resultados atuais e comportamentos futuros em `specs/001-solicitacao-reserva-idempotente/quickstart.md`
- [ ] T027 Executar a suite e validar inicializacao com PostgreSQL conteinerizado, registrando eventuais ajustes de execucao em `specs/001-solicitacao-reserva-idempotente/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Pode iniciar imediatamente.
- **Foundational (Phase 2)**: Depende de Setup e bloqueia todas as historias.
- **User Story 1 (Phase 3)**: Depende da fundacao; produz o MVP de confirmacao.
- **User Story 2 (Phase 4)**: Depende da fundacao e do DTO/controller introduzido por US1.
- **User Story 3 (Phase 5)**: Depende da fundacao e do endpoint introduzido por US1; integra o erro compartilhado de US2 se as historias forem executadas sequencialmente.
- **Polish (Phase 6)**: Depende das historias selecionadas para a entrega.

### User Story Completion Order

```text
Setup -> Foundational -> US1 (MVP)
                         |-> US2
                         `-> US3
US1 + US2 + US3 -> Polish
```

- **US1** e obrigatoria para o endpoint confirmar uma vaga e constitui o MVP.
- **US2** amplia o endpoint com rejeicao de entrada; pode ter testes escritos em paralelo a US1, mas sua integracao modifica DTO/controller de US1.
- **US3** amplia o endpoint com conflito simples e verifica a restricao criada na fundacao; nao inclui tratamento de corrida simultanea.

### Within Each User Story

- Escrever os testes da historia antes da implementacao e verificar falha inicial.
- Implementar DTOs/erros antes do mapeamento HTTP que os utiliza.
- Implementar regras de aplicacao antes de concluir os cenarios integrados.
- Nao adicionar replay idempotente ou rejeicao executavel de `REQUEST_ID_INCONSISTENTE` nesta entrega.

## Parallel Opportunities

- Em Setup, `T002`, `T003` e `T004` podem ocorrer em paralelo apos `T001`.
- Em Foundational, `T006` e `T009` podem avancar em arquivos distintos enquanto `T005` define o schema; `T007` depende do schema e enums, e `T008` depende das entidades.
- Em US1, `T011`, `T012` e `T013` podem ser preparados em paralelo antes de integrar service/controller.
- Em US3, `T020`, `T021`, `T022` e `T023` cobrem arquivos distintos antes das alteracoes sequenciais em service e handler.

## Parallel Example: User Story 1

```text
Task: "Criar teste de contrato para resposta 201 CONFIRMADA em src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaSuccessContractTest.java"
Task: "Criar teste de integracao da confirmacao em src/test/java/com/mateuspaz/reservas/reserva/application/ConfirmarReservaIntegrationTest.java"
Task: "Criar DTOs HTTP em src/main/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaRequest.java e ReservaConfirmadaResponse.java"
```

## Parallel Example: User Story 2

```text
Task: "Criar testes de validacao em src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaValidationContractTest.java"
Task: "Criar modelos de erro em src/main/java/com/mateuspaz/reservas/reserva/api/ApiErrorDetail.java e ValidationErrorResponse.java"
```

## Parallel Example: User Story 3

```text
Task: "Criar teste HTTP de conflito em src/test/java/com/mateuspaz/reservas/reserva/api/SolicitacaoReservaConflictContractTest.java"
Task: "Criar teste da restricao PostgreSQL em src/test/java/com/mateuspaz/reservas/reserva/persistence/ReservaConfirmationConstraintIntegrationTest.java"
Task: "Criar teste integrado de vaga confirmada em src/test/java/com/mateuspaz/reservas/reserva/application/RejeitarVagaConfirmadaIntegrationTest.java"
Task: "Criar resposta/excecao de conflito em src/main/java/com/mateuspaz/reservas/reserva/api/ConflictErrorResponse.java e src/main/java/com/mateuspaz/reservas/reserva/application/VagaEmConflitoException.java"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Concluir Setup e Foundational, incluindo migrations e PostgreSQL de teste.
2. Implementar US1 com resposta `201 CONFIRMADA`.
3. Executar os testes de US1 e demonstrar a confirmacao valida.

### Incremental Delivery

1. Adicionar US2 para responder `400 VALIDATION_ERROR` a entradas incorretas.
2. Adicionar US3 para responder `409 VAGA_EM_CONFLITO` e provar a restricao persistente.
3. Executar Polish e validar o quickstart contra PostgreSQL.
4. Planejar a fase posterior para replay idempotente, `REQUEST_ID_INCONSISTENTE` executavel e corridas simultaneas.

## Notes

- Tarefas `[P]` atuam em arquivos diferentes e podem ser preparadas em paralelo conforme dependencias indicadas.
- Tarefas `[US1]`, `[US2]` e `[US3]` sao rastreaveis aos cenarios da especificacao.
- O contrato OpenAPI inclui `REQUEST_ID_INCONSISTENTE` para estabilidade futura; sua implementacao nao pertence a este `tasks.md`.
