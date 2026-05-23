# Fases de Implementação da PoC de Reservas

## Visão geral

Este documento organiza a implementação incremental da PoC do módulo de reservas. A entrega deve comprovar que uma vaga não pode ser confirmada por mais de uma solicitação concorrente e que o reenvio de uma mesma requisição produz uma resposta repetível, sem efeitos colaterais adicionais.

A abordagem prioriza primeiro as garantias funcionais e transacionais essenciais. As fases posteriores acrescentam observabilidade, integração assíncrona e otimização sem transferir a responsabilidade pela consistência para componentes auxiliares.

## Contratos e decisões arquiteturais

- O endpoint principal é `POST /reservas`, recebendo `vagaId`, `clienteId` e `requestId`.
- A reserva vencedora confirma diretamente a vaga e retorna `201 Created`.
- Tentativas concorrentes para uma vaga já confirmada retornam `409 Conflict`.
- Reenvios com o mesmo `requestId` e o mesmo payload retornam o mesmo status HTTP e o mesmo corpo obtidos na primeira conclusão da requisição.
- O reuso de um `requestId` com payload diferente é rejeitado sem iniciar novo processamento.
- PostgreSQL é a fonte de verdade para a confirmação única por vaga e para o resultado idempotente persistido.
- RabbitMQ transporta eventos gerados após a decisão da reserva; ele não torna o endpoint principal assíncrono.
- Redis atua como cache otimista para reduzir processamento repetido; indisponibilidade ou perda do cache não afeta a corretude.
- Para a PoC, o fluxo exposto pela API considera a transição efetiva de vaga livre para reserva `CONFIRMADO`, sem exigir uma etapa intermediária observável de reserva pendente.

## Fase 1 - Fundação da aplicação e contrato da API

### Objetivo

Preparar uma base executável e persistente para a PoC, com contrato de entrada e saída definido antes da implementação das regras concorrentes.

### Especificação de negócio

O sistema deve disponibilizar uma operação de solicitação de reserva claramente definida para os clientes integradores. Cada solicitação deve identificar a vaga desejada, o cliente que a solicita e um identificador único de requisição que permita reconhecer repetições.

Nesta fase, devem estar definidos os resultados observáveis da operação: confirmação bem-sucedida, rejeição por conflito da vaga, erro de validação e rejeição por uso inconsistente do identificador de requisição.

### Descrição técnica

- Utilizar Java 21 e Spring Boot como base da aplicação REST.
- Utilizar Spring Web para exposição do endpoint e validação do contrato HTTP.
- Utilizar Spring Data JPA para acesso persistente e PostgreSQL como banco relacional da aplicação.
- Utilizar Flyway para controlar a evolução do schema desde o início da PoC.
- Modelar as informações necessárias para vaga, reserva confirmada e registro do resultado associado ao `requestId`.
- Definir os formatos de requisição, resposta de sucesso e respostas de erro de forma estável para as fases seguintes.
- Preparar configuração de execução local e conteinerizada da aplicação com PostgreSQL.

### Critérios de conclusão

- A aplicação inicializa conectada a um PostgreSQL com migrations versionadas aplicadas.
- O contrato de `POST /reservas` está definido para solicitações válidas e inválidas.
- O modelo persistente suporta a implementação posterior de confirmação única e idempotência.

## Fase 2 - Confirmação atômica e idempotência persistente

### Objetivo

Entregar o comportamento central da PoC: somente uma reserva pode confirmar uma determinada vaga, e uma mesma requisição deve produzir sempre o resultado originalmente decidido.

### Especificação de negócio

Quando vários clientes tentarem reservar simultaneamente a mesma vaga, somente uma solicitação deve ser confirmada. Todas as demais devem receber um resultado de conflito, sem que o sistema apresente duplicidade de confirmações.

Quando um integrador reenviar a mesma solicitação devido a timeout, repetição de mensagem ou tentativa de recuperação, o sistema deve reconhecer o `requestId` e devolver o resultado previamente concluído. O reenvio não deve criar outra reserva nem competir novamente pela vaga.

Se um integrador reutilizar um `requestId` para solicitar dados diferentes dos enviados inicialmente, a operação deve ser rejeitada, pois o identificador deixa de representar a mesma intenção de negócio.

### Descrição técnica

- Implementar o processamento síncrono de `POST /reservas` em transação.
- Persistir o resultado final da requisição associado a `requestId`, incluindo dados necessários para reproduzir status HTTP e corpo da resposta.
- Manter uma assinatura ou os campos relevantes do payload vinculados ao registro idempotente para identificar reuso inconsistente do `requestId`.
- Serializar tentativas concorrentes que utilizem o mesmo `requestId`, evitando dois processamentos simultâneos da mesma intenção.
- Garantir no PostgreSQL a unicidade de reserva confirmada por `vagaId`, por restrição de integridade apropriada ao status confirmado.
- Traduzir a perda de uma disputa concorrente pela vaga em resposta de conflito de negócio, sem expor detalhes internos do banco.
- Manter o registro idempotente do resultado de conflito, permitindo que o replay de uma solicitação perdedora também seja repetível.

### Critérios de conclusão

- Uma vaga não possui mais de uma reserva em estado `CONFIRMADO`.
- Tentativas concorrentes resultam em uma única resposta de confirmação e respostas de conflito para as demais.
- Reenvios de solicitações bem-sucedidas ou conflitantes repetem status e corpo sem novo efeito colateral.
- Reuso de `requestId` com payload divergente é rejeitado de forma determinística.

## Fase 3 - Observabilidade e comprovação dos cenários concorrentes

### Objetivo

Produzir evidências operacionais e automatizadas de que as garantias de concorrência e idempotência funcionam em execuções reais.

### Especificação de negócio

A demonstração da PoC deve permitir auditar quais requisições disputaram uma vaga, qual delas foi confirmada, quais foram rejeitadas por conflito e quais foram atendidas como repetições idempotentes.

Também deve ser possível medir a frequência de conflitos, a frequência de reenvios reconhecidos e o tempo de processamento, apoiando a avaliação de confiabilidade do módulo.

### Descrição técnica

- Produzir logs estruturados contendo correlação por `requestId`, `vagaId`, resultado da operação e indicação de replay idempotente.
- Utilizar o mecanismo de logging do ecossistema Spring com propagação controlada de contexto de requisição.
- Utilizar Spring Boot Actuator e Micrometer para instrumentar contadores de confirmação, conflito e replay, além da duração do processamento.
- Expor métricas em formato compatível com Prometheus para coleta e visualização operacional.
- Executar testes de integração contra PostgreSQL real utilizando Testcontainers, para validar as mesmas garantias disponíveis no banco adotado pela aplicação.
- Incluir teste concorrente com pelo menos 20 solicitações simultâneas e `requestId` distintos para uma mesma vaga.
- Registrar instruções de execução da PoC e de reprodução dos cenários de validação.

### Critérios de conclusão

- Os testes concorrentes demonstram zero ocorrências de duas reservas confirmadas para a mesma vaga.
- Testes automatizados demonstram replay idêntico para respostas de sucesso e conflito.
- Logs permitem identificar requisições vencedoras, perdedoras e replays.
- Métricas permitem acompanhar confirmações, conflitos, replays e latência do processamento.

## Fase 4 - Publicação de eventos pós-decisão com RabbitMQ

### Objetivo

Adicionar integração assíncrona aos resultados da reserva sem alterar o contrato síncrono nem as garantias transacionais já comprovadas.

### Especificação de negócio

Uma reserva confirmada ou rejeitada pode gerar eventos para notificação, auditoria ampliada ou integração com outros módulos. O cliente que solicitou a reserva, entretanto, deve continuar recebendo a decisão no próprio retorno do endpoint, sem depender do processamento posterior desses eventos.

Uma falha temporária no mecanismo de mensagens não pode desfazer uma reserva confirmada, permitir overbooking ou transformar uma resposta já entregue ao cliente.

### Descrição técnica

- Utilizar RabbitMQ como broker de eventos relacionados ao resultado da operação de reserva.
- Adotar o padrão transactional outbox para registrar o evento na mesma transação que persiste a decisão autoritativa no PostgreSQL.
- Publicar mensagens da outbox para o RabbitMQ após a confirmação da transação, com mecanismo de novas tentativas em caso de indisponibilidade do broker.
- Incluir identificadores de correlação, como `requestId` e identificador da reserva, no evento publicado.
- Exigir consumo idempotente dos eventos, pois publicações ou entregas podem ocorrer mais de uma vez.
- Manter o `POST /reservas` independente da disponibilidade imediata do RabbitMQ para definir sua resposta de confirmação ou conflito.

### Critérios de conclusão

- Resultados de reserva geram eventos publicáveis sem alterar o comportamento HTTP definido nas fases anteriores.
- Indisponibilidade do RabbitMQ não provoca perda da decisão persistida nem duplicidade de confirmação.
- Eventos não publicados imediatamente permanecem recuperáveis para reenvio.
- O processamento repetido de um evento não gera efeitos adicionais indevidos.

## Fase 5 - Cache otimista com Redis e entrega demonstrável

### Objetivo

Otimizar caminhos frequentes e empacotar a PoC para demonstração completa, preservando PostgreSQL como autoridade final do negócio.

### Especificação de negócio

Solicitações repetidas e tentativas para vagas que já foram confirmadas podem ser atendidas com menor latência, desde que o resultado continue consistente com a decisão registrada pelo sistema.

A demonstração final deve poder ser executada em ambiente controlado com os componentes previstos, permitindo comprovar o comportamento funcional, a observabilidade e as integrações adicionadas.

### Descrição técnica

- Utilizar Redis para armazenar em cache resultados idempotentes já concluídos, acelerando reenvios de `requestId` conhecidos.
- Utilizar Redis como indicador de vagas já confirmadas para rejeitar rapidamente tentativas evidentemente inviáveis.
- Aplicar estratégia de cache-aside: resultados retornados a partir do cache devem refletir decisões previamente persistidas no PostgreSQL.
- Em caso de ausência, expiração ou indisponibilidade do cache, prosseguir pelo fluxo autoritativo no PostgreSQL.
- Definir expiração das chaves de cache compatível com o período de demonstração e com a política de repetição da PoC.
- Manter a verificação transacional do PostgreSQL mesmo quando o cache não apontar conflito, evitando que misses ou concorrência no Redis permitam overbooking.
- Disponibilizar Dockerfile e configuração conteinerizada para executar aplicação, PostgreSQL, RabbitMQ, Redis e componentes de coleta de métricas necessários à demonstração.
- Preparar configuração de implantação em ambiente de homologação para execução dos cenários documentados.

### Critérios de conclusão

- Caminhos de replay e conflito conhecido podem ser atendidos com uso do cache sem alterar respostas funcionais.
- A aplicação preserva consistência quando Redis estiver vazio, expirado ou indisponível.
- O pacote conteinerizado permite executar os testes e a demonstração com os componentes previstos.
- A PoC está disponível em homologação com orientação para reprodução dos cenários principais.

## Cenários de validação da entrega

- Solicitação válida para vaga livre retorna reserva confirmada.
- Duas ou mais solicitações concorrentes com `requestId` distintos para a mesma vaga produzem somente uma confirmação.
- Vinte ou mais solicitações simultâneas para a mesma vaga não geram duplicidade de reserva confirmada.
- Reenvio de uma solicitação confirmada repete o mesmo status HTTP e corpo sem criar nova reserva.
- Reenvio de uma solicitação rejeitada por conflito repete o mesmo status HTTP e corpo sem nova tentativa de confirmação.
- Envio do mesmo `requestId` com `vagaId` ou `clienteId` diferentes é rejeitado.
- Falha de publicação ou entrega duplicada de eventos RabbitMQ não altera a reserva persistida.
- Indisponibilidade ou perda de dados no Redis não modifica a corretude do processamento.

## Premissas

- A PoC prioriza a demonstração de consistência e idempotência antes de otimizar desempenho ou integrar consumidores externos.
- RabbitMQ e Redis fazem parte da entrega faseada, mas não substituem as garantias transacionais fornecidas pelo PostgreSQL.
- O endpoint principal permanece síncrono em todas as fases.
- A evolução futura do módulo pode introduzir novos fluxos de negócio, mas estes não fazem parte desta implementação faseada.
