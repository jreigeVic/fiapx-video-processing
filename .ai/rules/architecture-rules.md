# Architecture Rules

Estas regras nunca podem ser violadas.

## Comunicação

Preferencialmente assíncrona.

## Banco

Cada serviço é dono do próprio banco.

Nenhum serviço pode atualizar banco de outro serviço.

## Eventos

Todo processamento pesado deve ser orientado a eventos.

## Worker

O Worker nunca altera diretamente o Video Service.

Ele publica eventos.

## Serviços

Toda regra de negócio pertence ao domínio.

## Cloud

Sempre preferir serviços gerenciados AWS.

## Arquitetura

Sempre utilizar:

- Clean
- Hexagonal
- Ports & Adapters