# Feature Specification: Solicitacao de Reserva Idempotente

**Feature Branch**: `001-solicitacao-reserva-idempotente`

**Created**: 2026-05-23

**Status**: Draft

**Input**: User description: "O sistema deve disponibilizar uma operacao de solicitacao de reserva claramente definida para os clientes integradores. Cada solicitacao deve identificar a vaga desejada, o cliente que a solicita e um identificador unico de requisicao que permita reconhecer repeticoes. Nesta fase, devem estar definidos os resultados observaveis da operacao: confirmacao bem-sucedida, rejeicao por conflito da vaga, erro de validacao e rejeicao por uso inconsistente do identificador de requisicao."

## Clarifications

### Session 2026-05-23

- Q: A feature atual deve entregar o comportamento completo da reserva ou apenas a fundacao contratual e persistente para implementa-lo na proxima fase? -> A: Implementar agora confirmacao e validacao; deixar conflito concorrente e idempotencia para a fase seguinte.
- Q: Nesta entrega, o que deve ocorrer quando uma solicitacao valida tentar reservar uma vaga que ja esta confirmada antes do processamento comecar? -> A: Retornar rejeicao por conflito da vaga; apenas a garantia sob concorrencia simultanea fica para a fase seguinte.
- Q: A migration desta entrega deve impedir fisicamente duas reservas confirmadas para a mesma vaga, mesmo antes da fase de testes e tratamento completo de concorrencia? -> A: Sim; incluir a restricao persistente agora e implementar o tratamento completo da corrida na fase seguinte.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Solicitar uma reserva valida (Priority: P1)

Como cliente integrador, quero solicitar uma reserva indicando a vaga, o cliente solicitante e um identificador unico de requisicao, para obter uma confirmacao identificavel nesta entrega.

**Why this priority**: A confirmacao de uma solicitacao valida e o fluxo executavel principal desta entrega e estabelece o resultado de sucesso do contrato.

**Independent Test**: Pode ser testada enviando uma solicitacao valida para uma vaga disponivel; o resultado confirma a reserva e identifica a vaga, o cliente e o identificador informado.

**Acceptance Scenarios**:

1. **Given** uma vaga apta a receber a reserva e dados validos de cliente e identificador de requisicao ainda nao utilizado, **When** o cliente integrador solicita a reserva, **Then** recebe uma confirmacao bem-sucedida vinculada a vaga, ao cliente e ao identificador informado.

---

### User Story 2 - Rejeitar solicitacao invalida (Priority: P2)

Como cliente integrador, quero receber erro claro quando nao informar corretamente vaga, cliente ou identificador de requisicao, para corrigir a chamada sem presumir uma reserva.

**Why this priority**: A validacao e o segundo comportamento executavel desta entrega e impede que entradas incompletas sejam confundidas com confirmacoes.

**Independent Test**: Pode ser testada enviando solicitacoes com cada campo obrigatorio ausente ou invalido; todas retornam erro de validacao com os dados a corrigir.

**Acceptance Scenarios**:

1. **Given** uma solicitacao sem vaga, sem cliente, sem identificador de requisicao ou com valor invalido em qualquer desses campos, **When** ela e apresentada, **Then** o integrador recebe erro de validacao que identifica quais dados impedem a solicitacao de ser avaliada.

---

### User Story 3 - Rejeitar vaga ja indisponivel (Priority: P3)

Como cliente integrador, quero receber conflito quando solicitar uma vaga que ja esteja confirmada antes do processamento, para nao tratar uma vaga indisponivel como nova confirmacao.

**Why this priority**: Esse conflito simples impede uma confirmacao incorreta nesta entrega; a garantia contra disputa simultanea e a idempotencia continuam reservadas para a fase seguinte.

**Independent Test**: Pode ser testada solicitando uma vaga previamente confirmada, sem chamadas simultaneas; o resultado e rejeicao por conflito e nao produz nova confirmacao.

**Acceptance Scenarios**:

1. **Given** uma vaga previamente confirmada e uma solicitacao valida processada sem disputa simultanea, **When** o integrador solicita essa vaga, **Then** recebe rejeicao por conflito da vaga e nenhuma reserva adicional e confirmada.
2. **Given** os formatos de resposta definidos para a operacao, **When** um integrador analisa as rejeicoes previstas, **Then** consegue distinguir rejeicao por conflito da vaga de rejeicao futura por uso inconsistente do identificador.

### Edge Cases

- Na fase seguinte, uma repeticao da solicitacao original ocorrera depois que seu resultado ja foi comunicado ao integrador; ela devera continuar associada ao resultado original, mesmo que a disponibilidade atual da vaga seja diferente.
- Na fase seguinte, um identificador previamente utilizado e reenviado com alteracao simultanea de vaga e cliente; o resultado devera ser rejeicao por uso inconsistente do identificador.
- Uma solicitacao combina campos ausentes ou invalidos com um identificador que nao pode ser reconhecido validamente; o resultado e erro de validacao, sem avaliar disponibilidade da vaga.
- Na fase seguinte, duas solicitacoes validas distintas disputarao a mesma vaga; no maximo uma podera obter confirmacao, e a outra devera receber rejeicao por conflito.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST disponibilizar aos clientes integradores uma operacao de solicitacao de reserva com resultados observaveis e distinguiveis.
- **FR-002**: Cada solicitacao MUST informar a vaga desejada, o cliente solicitante e um identificador unico de requisicao.
- **FR-003**: O sistema MUST produzir confirmacao bem-sucedida quando uma solicitacao valida, com identificador ainda nao utilizado, puder reservar a vaga desejada.
- **FR-004**: A confirmacao bem-sucedida MUST permitir ao integrador relacionar o resultado a vaga, ao cliente e ao identificador de requisicao apresentados.
- **FR-005**: O sistema MUST rejeitar como conflito da vaga uma solicitacao valida processada para vaga ja confirmada antes do inicio do processamento; a garantia para disputas simultaneas sera implementada na fase seguinte.
- **FR-006**: O sistema MUST produzir erro de validacao quando vaga, cliente ou identificador de requisicao estiver ausente ou invalido, indicando os dados que devem ser corrigidos.
- **FR-007**: O modelo persistente MUST suportar o reconhecimento futuro de uma repeticao quando o mesmo identificador de requisicao for reapresentado com a mesma vaga e o mesmo cliente da solicitacao original.
- **FR-008**: O contrato MUST definir que uma repeticao consistente apresentara resultado equivalente ao resultado originalmente associado ao identificador e nao confirmara reserva adicional quando a idempotencia for implementada na fase seguinte.
- **FR-009**: O contrato MUST definir rejeicao por uso inconsistente do identificador para a futura reapresentacao de identificador ja reconhecido acompanhada de vaga ou cliente diferente do original.
- **FR-010**: Uma rejeicao por conflito, validacao ou uso inconsistente do identificador MUST NOT ser apresentada como confirmacao de reserva.
- **FR-011**: O modelo persistente MUST impedir desde esta entrega que duas reservas confirmadas sejam registradas para a mesma vaga, mesmo que o tratamento controlado de disputas simultaneas seja implementado na fase seguinte.
- **FR-012**: Nesta entrega, o comportamento executavel da operacao MUST incluir confirmacao de solicitacao valida, erro de validacao e conflito para vaga previamente confirmada; a garantia sob concorrencia simultanea e a idempotencia por identificador MUST permanecer no escopo de implementacao da fase seguinte.

### Key Entities *(include if feature involves data)*

- **Solicitacao de Reserva**: Pedido feito por um cliente integrador; identifica a vaga desejada, o cliente solicitante e o identificador unico de requisicao.
- **Identificador de Requisicao**: Referencia unica fornecida pelo integrador para reconhecer repeticoes e impedir que dados diferentes sejam tratados como o mesmo pedido.
- **Vaga**: Recurso desejado na reserva, cuja aptidao para receber a solicitacao determina confirmacao ou conflito.
- **Resultado da Solicitacao**: Desfecho observavel associado a uma solicitacao: confirmacao bem-sucedida, rejeicao por conflito, erro de validacao ou rejeicao por uso inconsistente do identificador.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos cenarios executaveis e formatos contratuais definidos, o cliente integrador consegue distinguir se o resultado e confirmacao, conflito da vaga, erro de validacao ou uso inconsistente do identificador.
- **SC-002**: Em 100% dos testes executaveis desta entrega, solicitacoes validas confirmadas apresentam vaga, cliente e identificador informados, solicitacoes invalidas apresentam erro de validacao distinguivel e vagas previamente confirmadas apresentam conflito.
- **SC-003**: O contrato e o modelo persistente desta entrega representam, sem alteracao posterior de formato, os resultados futuros de repeticao consistente, uso inconsistente do identificador e conflito sob disputa simultanea.
- **SC-004**: Em uma amostra representativa de 100 solicitacoes individuais, pelo menos 95% apresentam seu resultado observavel ao integrador em ate 2 segundos.
- **SC-005**: Em avaliacao de entendimento com representantes de clientes integradores, pelo menos 90% identificam corretamente a acao a tomar para cada um dos quatro resultados, sem orientacao adicional.
- **SC-006**: Em testes da migration desta entrega, 100% das tentativas de registrar uma segunda reserva confirmada para a mesma vaga sao impedidas pelo modelo persistente.

## Assumptions

- O identificador de requisicao e unico no contexto da operacao de reserva e permanece associavel ao primeiro pedido valido reconhecido para permitir repeticoes posteriores.
- A aptidao de uma vaga para reserva e determinada por regras de disponibilidade ja definidas fora do escopo desta especificacao.
- Esta feature implementa confirmacao, validacao e conflito para vaga previamente confirmada; garantia sob concorrencia simultanea e idempotencia sao implementadas na fase seguinte.
- A migration inicial ja aplica a restricao persistente de no maximo uma reserva confirmada por vaga; a fase seguinte transforma perdas de disputa simultanea em resposta de negocio controlada.
- Alteracao, cancelamento, expiracao ou consulta de reservas nao fazem parte deste escopo.
- Os clientes integradores conseguem fornecer identificadores estaveis para vaga, cliente e requisicao em cada solicitacao.
