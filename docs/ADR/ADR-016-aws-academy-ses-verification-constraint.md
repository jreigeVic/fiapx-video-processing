# ADR-016 - Restricao de Verificacao de Identidades SES no AWS Academy

## Status

Approved

## Contexto

ADR-009 (Seguranca) e o HLD definem notificacao por e-mail via Amazon SES (RF-07). AWS Academy mantem as contas em modo sandbox do SES, exigindo que remetente e destinatarios sejam identidades verificadas.

`infrastructure/terraform/ses.tf` provisionava `aws_ses_email_identity` para criar e disparar a verificacao dessas identidades via Terraform.

## Problema

Ao rodar `terraform apply` em uma conta real do AWS Academy (epic/009-kubernetes), a criacao do `aws_ses_email_identity` falhou:

```
Error: requesting SES Email Identity (...) verification: operation error SES: VerifyEmailIdentity,
https response error StatusCode: 403, api error AccessDenied: User: arn:aws:sts::...:assumed-role/voclabs/...
is not authorized to perform: ses:VerifyEmailIdentity because no identity-based policy allows the ses:VerifyEmailIdentity action
```

A role `voclabs` fornecida pelo AWS Academy nega explicitamente a acao `ses:VerifyEmailIdentity`. Como essa e a mesma acao usada tanto pela API/Terraform quanto pelo botao "Verify a New Email Address" no Console SES, nao ha caminho alternativo dentro da conta de laboratorio para verificar identidades - a restricao e de IAM, nao apenas de automacao.

Sem esse recurso, todo `terraform apply` no AWS Academy falhava antes de concluir os demais recursos (EKS, RDS, S3, SNS/SQS, ECR), mesmo esses nao tendo nenhuma relacao com SES.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Manter o recurso obrigatorio e pedir para a Academy liberar a permissao | Fora de controle do time; prazo do hackathon nao permite depender disso. |
| Trocar o canal de notificacao de e-mail para outro mecanismo (ex.: apenas log/CloudWatch) | RF-07 aceita "e-mail ou outro canal", mas isso seria uma mudanca arquitetural nao solicitada - fora do escopo desta decisao (ver Fora de Escopo no roadmap de fechamento). |
| Tornar a criacao das identidades SES condicional (variavel `ses_manage_identities`, default `false`) | Mantem a decisao original (SES) intacta para contas com a permissao correta, desbloqueia o `apply` no AWS Academy sem alterar nenhum outro recurso. |

## Decisao

Adicionar a variavel `ses_manage_identities` (bool, default `false`) em `infrastructure/terraform/variables.tf`. O recurso `aws_ses_email_identity` em `infrastructure/terraform/ses.tf` so e criado quando essa variavel e `true`.

No AWS Academy, a variavel permanece `false`: a infraestrutura sobe completa (EKS, RDS, S3, SNS/SQS, ECR), mas nenhuma identidade SES e verificada automaticamente.

O `notification-service` nao foi alterado. Ele continua tentando enviar e-mails via SES normalmente; a rejeicao do SES sandbox (`MessageRejectedException`, identidade nao verificada) ja era tratada como falha permanente de entrega antes desta decisao (`EmailNotificationAdapter` + `NotifyVideoProcessedUseCase`/`NotifyVideoFailedUseCase`): a notificacao e persistida com status `FAILED` e o evento e confirmado (ack) - sem crash do servico, sem retry infinito, sem acumulo na DLQ por esse motivo especifico.

## Justificativa

Preserva a arquitetura aprovada (SES, RF-07, ADR-009) sem alteracao, isola a restricao de ambiente de laboratorio em uma unica variavel documentada, e nao exige nenhuma mudanca de codigo na aplicacao - o comportamento de falha ja era resiliente por design.

## Consequencias

- No ambiente AWS Academy, o envio de e-mails de notificacao (RF-07) nao pode ser demonstrado como entrega real fim-a-fim; a evidencia disponivel e o registro de `Notification` com status `FAILED` e os logs do `notification-service` tentando o envio.
- Em uma conta AWS com a permissao `ses:VerifyEmailIdentity` liberada (ex.: conta pessoal fora do Academy), basta definir `ses_manage_identities = true` para restaurar o comportamento original de verificacao automatica via Terraform.
- Nao foi confirmado se a role `voclabs` tambem nega `ses:SendEmail` isoladamente (a falha observada foi apenas em `ses:VerifyEmailIdentity`). Caso negue, o `notification-service` ainda permanece estavel: a excecao generica cai no tratamento padrao do `ProcessingNotificationConsumer`, que deixa a mensagem para redrive/DLQ do SQS.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| `terraform apply` completo e repetivel no AWS Academy | RF-07 nao e demonstravel via entrega real de e-mail neste ambiente |
| Nenhuma mudanca de arquitetura ou de codigo da aplicacao | Depende de evidencia indireta (registro `FAILED` + logs) para a banca |

## Referencias

- docs/ADR/ADR-009-security.md
- infrastructure/terraform/ses.tf
- infrastructure/terraform/variables.tf
- services/notification-service/src/main/java/com/fiapx/notification/infrastructure/adapter/out/EmailNotificationAdapter.java
- services/notification-service/src/main/java/com/fiapx/notification/application/usecase/NotifyVideoFailedUseCase.java
- services/notification-service/src/main/java/com/fiapx/notification/application/usecase/NotifyVideoProcessedUseCase.java
