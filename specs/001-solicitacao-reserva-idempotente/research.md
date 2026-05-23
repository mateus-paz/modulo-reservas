# Research: Solicitacao de Reserva Idempotente

## Decision 1: Runtime e framework base

**Decision**: Usar Java 21 com a versao Spring Boot 3.5.14 ja configurada no `pom.xml`, adicionando suporte explicito a Bean Validation para validar os DTOs de entrada.

**Rationale**: A base existente ja atende a direcao tecnica solicitada e reduz alteracoes sem valor funcional. Spring Web suporta a operacao REST; validacao declarativa torna as rejeicoes de entrada consistentes com o contrato.

**Alternatives considered**:

- Atualizar Spring Boot durante a fundacao: rejeitado porque nao e requisito da feature e ampliaria risco sem beneficio ao contrato.
- Validar campos manualmente no controller: rejeitado porque dispersa regras de formato e dificulta testes consistentes de erro.

## Decision 2: Banco relacional e evolucao do schema

**Decision**: Usar PostgreSQL como fonte de verdade da aplicacao e Flyway com migrations SQL versionadas desde a primeira estrutura persistente.

**Rationale**: O problema posterior exige restricoes e transacoes reais para confirmacao unica e idempotencia. A migration inicial fornece uma base reproduzivel em execucao local, container e testes.

**Alternatives considered**:

- Criar schema automaticamente pelo mapeamento persistente: rejeitado porque nao oferece evolucao auditavel da PoC.
- Usar H2 como banco principal ou como evidencia das restricoes: rejeitado porque nao comprova integralmente comportamento e recursos do PostgreSQL.

## Decision 3: Contrato de identificadores e endpoint

**Decision**: Definir `POST /reservas` com corpo JSON contendo `vagaId`, `clienteId` e `requestId`, todos no formato UUID. Nesta entrega, retornar `201 Created` para confirmacao, `400 Bad Request` para validacao e `409 Conflict` para vaga previamente confirmada; manter `409 Conflict` com codigo distinto para uso inconsistente de `requestId` no contrato da fase seguinte.

**Rationale**: O contrato usa identificadores opacos e uniformes, mapeia resultados observaveis da especificacao e separa entrada invalida de conflito de negocio. `409` permite ao integrador distinguir ambas as rejeicoes de negocio pelo codigo estavel.

**Alternatives considered**:

- Identificadores textuais sem formato: rejeitado por deixar validacao e interoperabilidade indefinidas.
- Usar `422` para validacao: possivel, mas `400` e suficiente e mais simples para a fundacao desta API.
- Responder todos os conflitos com um mesmo corpo indistinto: rejeitado porque viola a necessidade de reconhecer `requestId` inconsistente.

## Decision 4: Modelo persistente preparado para a fase seguinte

**Decision**: Modelar `vaga`, `reserva` e `resultado_requisicao`. `reserva` referencia vaga/cliente e suporta estado `CONFIRMADA`; `resultado_requisicao` conserva `requestId`, payload original, classe de resultado, status HTTP e corpo retornavel. A migration inicial inclui indice unico de reserva confirmada por vaga.

**Rationale**: Nesta entrega, o endpoint confirma solicitacoes validas e rejeita vaga ja confirmada; o schema impede duplicidade de confirmacao mesmo antes do tratamento controlado de corridas. Os dados de resultado mantem o caminho preparado para detectar reutilizacao inconsistente e reproduzir respostas na fase de idempotencia.

**Alternatives considered**:

- Armazenar somente reservas confirmadas: rejeitado porque nao reproduz conflitos nem detecta reutilizacao inconsistente sem reprocessamento.
- Resolver unicidade apenas em memoria ou na aplicacao: rejeitado porque nao protege contra concorrencia entre transacoes ou instancias.

## Decision 5: Verificacao em ambientes reais

**Decision**: Manter o teste de carregamento de contexto existente, mas testar migrations e restricoes de persistencia contra PostgreSQL conteinerizado; exercitar o contrato HTTP separadamente com testes da camada web.

**Rationale**: O contrato pode ser validado rapidamente de forma isolada, enquanto garantias relacionadas ao schema devem executar no mesmo tipo de banco da aplicacao.

**Alternatives considered**:

- Validar tudo apenas com H2 em modo PostgreSQL: rejeitado porque indices e comportamento transacional podem divergir.
- Adiar qualquer teste PostgreSQL ate a fase de concorrencia: rejeitado porque a conclusao desta fase exige migrations PostgreSQL aplicaveis.

## Decision 6: Limite de escopo da fundacao

**Decision**: Entregar aplicacao REST, PostgreSQL, migrations, modelo, contrato, confirmacao, validacao e conflito para vaga previamente confirmada. RabbitMQ, Redis, metricas operacionais, replay idempotente e tratamento de disputa simultanea ficam fora desta fase.

**Rationale**: As clarificacoes exigem comportamento basico verificavel e a protecao persistente contra duplicidade sem antecipar a coordenacao concorrente nem idempotencia da fase seguinte.

**Alternatives considered**:

- Incluir mensageria e cache desde a base: rejeitado por ampliar o escopo antes de provar consistencia no PostgreSQL.
