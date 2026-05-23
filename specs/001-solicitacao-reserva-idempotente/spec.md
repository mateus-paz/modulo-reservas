# Feature Specification: Solicitacao de Reserva Idempotente

**Feature Branch**: `001-solicitacao-reserva-idempotente`

**Created**: 2026-05-23

**Status**: Draft

**Input**: User description: "O sistema deve disponibilizar uma operacao de solicitacao de reserva claramente definida para os clientes integradores. Cada solicitacao deve identificar a vaga desejada, o cliente que a solicita e um identificador unico de requisicao que permita reconhecer repeticoes. Nesta fase, devem estar definidos os resultados observaveis da operacao: confirmacao bem-sucedida, rejeicao por conflito da vaga, erro de validacao e rejeicao por uso inconsistente do identificador de requisicao."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Solicitar e repetir uma reserva (Priority: P1)

Como cliente integrador, quero solicitar uma reserva indicando a vaga, o cliente solicitante e um identificador unico de requisicao, para obter uma confirmacao confiavel e poder repetir a mesma solicitacao sem gerar uma reserva adicional.

**Why this priority**: A confirmacao e o reconhecimento de repeticoes constituem o fluxo principal e evitam reservas duplicadas quando um integrador precisa reenviar uma solicitacao.

**Independent Test**: Pode ser testada enviando uma solicitacao valida para uma vaga disponivel e repetindo exatamente os mesmos dados com o mesmo identificador; o resultado confirma a reserva uma unica vez e reconhece a repeticao.

**Acceptance Scenarios**:

1. **Given** uma vaga apta a receber a reserva e dados validos de cliente e identificador de requisicao ainda nao utilizado, **When** o cliente integrador solicita a reserva, **Then** recebe uma confirmacao bem-sucedida vinculada a vaga, ao cliente e ao identificador informado.
2. **Given** uma solicitacao que ja recebeu resultado para uma combinacao de vaga, cliente e identificador de requisicao, **When** o integrador repete a solicitacao com os mesmos dados, **Then** recebe o mesmo resultado observavel da solicitacao original e nenhuma nova reserva e confirmada.

---

### User Story 2 - Identificar conflito de vaga (Priority: P2)

Como cliente integrador, quero receber uma rejeicao inequivoca quando a vaga desejada nao puder ser reservada, para orientar o cliente sem presumir uma confirmacao inexistente.

**Why this priority**: A disputa por uma vaga e uma condicao esperada do negocio e precisa ter um desfecho distinto de erro nos dados enviados.

**Independent Test**: Pode ser testada solicitando uma vaga que ja esteja indisponivel segundo as regras vigentes de reserva; o resultado e identificado como conflito e nao confirma reserva.

**Acceptance Scenarios**:

1. **Given** uma vaga que nao esta apta a receber a reserva solicitada e uma solicitacao valida com identificador ainda nao utilizado, **When** o integrador solicita a reserva, **Then** recebe rejeicao por conflito da vaga e nenhuma reserva e confirmada para a solicitacao.
2. **Given** uma solicitacao valida rejeitada por conflito e registrada sob seu identificador, **When** a mesma solicitacao e repetida, **Then** a repeticao permanece reconhecivel como o mesmo resultado de conflito, sem nova confirmacao.

---

### User Story 3 - Distinguir dados invalidos e uso inconsistente (Priority: P3)

Como cliente integrador, quero distinguir uma solicitacao invalida da reutilizacao incorreta de um identificador, para corrigir os dados apropriados sem criar ambiguidade sobre reservas anteriores.

**Why this priority**: Entradas incorretas devem ser tratadas de modo previsivel, especialmente quando poderiam associar uma mesma requisicao a clientes ou vagas diferentes.

**Independent Test**: Pode ser testada primeiro enviando dados obrigatorios ausentes ou invalidos e depois reutilizando um identificador ja reconhecido com vaga ou cliente diferente; cada caso apresenta sua rejeicao especifica.

**Acceptance Scenarios**:

1. **Given** uma solicitacao sem vaga, sem cliente, sem identificador de requisicao ou com valor invalido em qualquer desses campos, **When** ela e apresentada, **Then** o integrador recebe erro de validacao que identifica quais dados impedem a solicitacao de ser avaliada.
2. **Given** um identificador de requisicao previamente associado a determinada vaga e cliente, **When** o integrador o reutiliza informando outra vaga ou outro cliente, **Then** recebe rejeicao por uso inconsistente do identificador e nenhuma reserva adicional e confirmada.

### Edge Cases

- Uma repeticao da solicitacao original ocorre depois que seu resultado ja foi comunicado ao integrador; ela deve continuar associada ao resultado original, mesmo que a disponibilidade atual da vaga seja diferente.
- O identificador previamente utilizado e reenviado com alteracao simultanea de vaga e cliente; o resultado continua sendo rejeicao por uso inconsistente do identificador.
- Uma solicitacao combina campos ausentes ou invalidos com um identificador que nao pode ser reconhecido validamente; o resultado e erro de validacao, sem avaliar disponibilidade da vaga.
- Duas solicitacoes validas distintas disputam a mesma vaga; no maximo uma pode obter confirmacao, e a outra recebe rejeicao por conflito.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST disponibilizar aos clientes integradores uma operacao de solicitacao de reserva com resultados observaveis e distinguiveis.
- **FR-002**: Cada solicitacao MUST informar a vaga desejada, o cliente solicitante e um identificador unico de requisicao.
- **FR-003**: O sistema MUST produzir confirmacao bem-sucedida quando uma solicitacao valida, com identificador ainda nao utilizado, puder reservar a vaga desejada.
- **FR-004**: A confirmacao bem-sucedida MUST permitir ao integrador relacionar o resultado a vaga, ao cliente e ao identificador de requisicao apresentados.
- **FR-005**: O sistema MUST rejeitar como conflito da vaga uma solicitacao valida quando a vaga desejada nao puder receber a reserva segundo as regras de disponibilidade aplicaveis.
- **FR-006**: O sistema MUST produzir erro de validacao quando vaga, cliente ou identificador de requisicao estiver ausente ou invalido, indicando os dados que devem ser corrigidos.
- **FR-007**: O sistema MUST reconhecer uma repeticao quando o mesmo identificador de requisicao for reapresentado com a mesma vaga e o mesmo cliente da solicitacao original.
- **FR-008**: Uma repeticao consistente MUST apresentar resultado equivalente ao resultado originalmente associado ao identificador e MUST NOT confirmar uma reserva adicional.
- **FR-009**: O sistema MUST rejeitar como uso inconsistente do identificador de requisicao qualquer reapresentacao de um identificador ja reconhecido acompanhada de vaga ou cliente diferente do original.
- **FR-010**: Uma rejeicao por conflito, validacao ou uso inconsistente do identificador MUST NOT ser apresentada como confirmacao de reserva.
- **FR-011**: Para solicitacoes validas distintas que disputam a mesma vaga, o sistema MUST NOT confirmar mais de uma reserva incompativel segundo as regras de disponibilidade aplicaveis.

### Key Entities *(include if feature involves data)*

- **Solicitacao de Reserva**: Pedido feito por um cliente integrador; identifica a vaga desejada, o cliente solicitante e o identificador unico de requisicao.
- **Identificador de Requisicao**: Referencia unica fornecida pelo integrador para reconhecer repeticoes e impedir que dados diferentes sejam tratados como o mesmo pedido.
- **Vaga**: Recurso desejado na reserva, cuja aptidao para receber a solicitacao determina confirmacao ou conflito.
- **Resultado da Solicitacao**: Desfecho observavel associado a uma solicitacao: confirmacao bem-sucedida, rejeicao por conflito, erro de validacao ou rejeicao por uso inconsistente do identificador.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos cenarios de aceite definidos, o cliente integrador consegue distinguir se o resultado foi confirmacao, conflito da vaga, erro de validacao ou uso inconsistente do identificador.
- **SC-002**: Em testes com repeticoes consistentes de solicitacoes previamente tratadas, 100% das repeticoes devolvem resultado equivalente ao original e zero reservas adicionais sao confirmadas.
- **SC-003**: Em testes com identificadores previamente usados para outra vaga ou outro cliente, 100% das tentativas sao rejeitadas como uso inconsistente e zero confirmacoes adicionais ocorrem.
- **SC-004**: Em uma amostra representativa de 100 solicitacoes individuais, pelo menos 95% apresentam seu resultado observavel ao integrador em ate 2 segundos.
- **SC-005**: Em avaliacao de entendimento com representantes de clientes integradores, pelo menos 90% identificam corretamente a acao a tomar para cada um dos quatro resultados, sem orientacao adicional.

## Assumptions

- O identificador de requisicao e unico no contexto da operacao de reserva e permanece associavel ao primeiro pedido valido reconhecido para permitir repeticoes posteriores.
- A aptidao de uma vaga para reserva e determinada por regras de disponibilidade ja definidas fora do escopo desta especificacao.
- Esta feature define a solicitacao e seus resultados observaveis; alteracao, cancelamento, expiracao ou consulta de reservas nao fazem parte deste escopo.
- Os clientes integradores conseguem fornecer identificadores estaveis para vaga, cliente e requisicao em cada solicitacao.
