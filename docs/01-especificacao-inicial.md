# [Módulo Reservas] PoC de concorrência e idempotência em reservas

## Visão Geral
Provar, por meio de uma Prova de Conceito (PoC), que o sistema de reservas é capaz de lidar com múltiplas requisições concorrentes para a mesma vaga/assento, garantindo no máximo uma reserva confirmada por vaga e comportamento idempotente quando o mesmo requestId é reenviado.

## Valor de Negócio
- Evitar overbooking e inconsistências de estoque de assentos
- Aumentar a confiabilidade da API de reservas sob alta concorrência
- Reduzir retrabalho e falhas operacionais em integrações de parceiros

## Resultados Esperados
- Demonstração funcional com endpoint POST /reservas aceitando vagaId, clienteId, requestId
- Garantia de no máximo uma transição para status CONFIRMADO por vaga
- Idempotência por requestId com resposta repetível
- logs para observabilidade dos cenários concorrentes

## Histórias Relacionadas
- Como cliente do sistema de reservas, quero reservar uma vaga com segurança sob concorrência, para evitar overbooking (esta história – PoC)

## Métricas de Sucesso
- 0 ocorrências de duas reservas CONFIRMADO para a mesma vaga em testes de concorrência
- Requisições repetidas com o mesmo requestId retornando o mesmo resultado em 100% dos casos
- Como sistema de reservas, quero tratar concorrência e idempotência ao reservar vagas, para garantir no máximo uma reserva confirmada por vaga e evitar overbooking

## História
A PoC deve expor POST /reservas com corpo { "vagaId": "...", "clienteId": "...", "requestId": "UUID" }. Cada vaga/assento possui id e status (LIVRE, RESERVADO, CONFIRMADO). O sistema deve:

- Suportar múltiplas requisições simultâneas para a mesma vaga, permitindo apenas uma confirmação por vaga.
- Implementar idempotência baseada em requestId: reenvios com o mesmo requestId devem retornar exatamente o mesmo resultado da primeira chamada.
- Garantir ausência de estados inválidos (sem dois registros CONFIRMADO para a mesma vaga).

(Template e estrutura de História conforme diretrizes.  )

## Cenários (BDD)
### Cenário 1: Concorrência real com requestId diferentes
- Dado que existem várias requisições quase simultâneas para a mesma vagaId
- E que cada requisição usa um requestId único
- Quando o sistema processa as requisições de criação em POST /reservas
- Então exatamente uma reserva deve alcançar status CONFIRMADO para aquela vagaId
- E as demais devem falhar com erro de negócio apropriado (por exemplo, 409 Conflict) ou retornar estado não confirmado

### Cenário 2: Idempotência para o mesmo requestId
- Dado que uma requisição válida foi enviada para POST /reservas com determinado requestId
- Quando a mesma requisição é reenviada com o mesmo requestId
- Então o sistema deve retornar o mesmo status e payload da primeira resposta (idempotente)
- E nenhum efeito colateral adicional deve ocorrer

### Cenário 3: Garantia de estados válidos
- Dado que o status possível da vaga é um dentre LIVRE, RESERVADO, CONFIRMADO
- Quando múltiplas tentativas de confirmação ocorrem para a mesma vagaId
- Então não deve existir mais de um registro em CONFIRMADO para a mesma vagaId
- E transições parciais devem ser revertidas/ignoradas, preservando consistência

## Critérios de Aceitação
- Existe endpoint POST /reservas que recebe vagaId, clienteId, requestId
- Em testes de concorrência (≥ 20 requisições simultâneas para a mesma vagaId), somente uma resposta resulta em CONFIRMADO
- Reenvio com o mesmo requestId retorna o mesmo resultado (HTTP status e corpo) da primeira chamada
- Não há dois registros CONFIRMADO para a mesma vagaId em nenhuma execução
- Logs e métricas permitem auditar tentativas, vencedores/perdedores de corrida e replays idempotentes

## Definição de Pronto
- Código revisado (code review) e testes unitários cobrindo fluxos de concorrência e idempotência
- Testes de integração exercitando cenários com carga (concorrência) executados e aprovados
- Observabilidade: logs estruturados e métricas básicas (taxa de conflito, taxa de replay idempotente)
- Documentação da PoC (README) explicando decisões técnicas, como reproduzir testes e limitações
- Deploy da PoC em ambiente de homologação para demonstração

## Subtarefas
### [Backend] Implementar chaves de idempotência por requestId
Descrição: Persistir primeiro resultado por requestId e garantir retorno repetível para replays.

Tarefas:
- Criar armazenamento de resultados por requestId (ex.: tabela idempotency_keys)
- Garantir upsert/lock por requestId durante o primeiro processamento
- Retornar resposta idêntica em reenvios

Critérios de Aceitação: Reenvio com mesmo requestId retorna mesmo status/corpo, sem efeitos colaterais.

### [Backend] Controlar concorrência para confirmação única por vagaId
Descrição: Assegurar que apenas uma requisição alcance CONFIRMADO por vaga.

Tarefas:
- Definir estratégia (ex.: bloqueio otimista/pessimista, unique constraint em (vagaId, status=CONFIRMADO), fila transacional)
- Implementar transação atômica na confirmação
- Retornar 409/erro de negócio para perdas de corrida

Critérios de Aceitação: Em carga concorrente, apenas um CONFIRMADO por vagaId.

### [Backend] Endpoint POST /reservas (PoC)
Descrição: Expor endpoint e orquestrar lógica de idempotência e concorrência.

Tarefas:
- Validar payload (vagaId, clienteId, requestId obrigatório, UUID)
- Integrar com camadas de idempotência e confirmação atômica
- Definir contratos de resposta (201 Created ao confirmar; 409 em conflito; 200 para replay idempotente)

Critérios de Aceitação: Endpoint funcional conforme critérios de aceitação da história.

### [Observabilidade] Logs e métricas da PoC
Descrição: Instrumentar logs estruturados e contadores.

Tarefas:
- Logar início/fim por requestId, vagaId, resultado (ganhou/perdeu corrida)
- Criar métricas: taxa de conflito, taxa de replay, duração p95
- Dashboard simples (ex.: Prometheus/Grafana ou logs consultáveis)

Critérios de Aceitação: Métricas/logs acessíveis e úteis para auditoria.

### [QA] Testes de concorrência e idempotência
Descrição: Validar cenários com carga e replays.

Tarefas:
- Script de carga disparando N requisições simultâneas para mesma vagaId
- Teste de reenvio com mesmo requestId
- Verificação automática de inexistência de mais de um CONFIRMADO por vagaId

Critérios de Aceitação: Todos os cenários passam; relatórios anexados.

## Stack sugerida
De stack, pode ser um spring mesmo, um postgress, um redis e um rabbitmq (pra vc se diverti com ele)

## Entregável
Dockerfile
