# ☁️ Infrastructure

Infraestrutura como código da plataforma FIAP X no AWS Academy (Learner Lab), além do provisionamento do LocalStack usado em desenvolvimento local e no CI.

| Diretório | Propósito |
|---|---|
| `terraform/` | Provisionamento AWS: ECR, RDS, S3, SNS/SQS (+DLQs), SES, EKS |
| `helm/` | Charts de deploy no Kubernetes (Helm) - veja [`helm/README.md`](helm/README.md) |
| `kubernetes/` | Manifestos e scripts de bootstrap do cluster |
| `localstack/` | Script de inicialização do LocalStack usado pelo docker-compose e pelo CI |
| `docker/` | Suporte a containers para desenvolvimento local (init do Postgres) |

## 🚧 Restrições do AWS Academy (por que o Terraform é assim)

- **Sem criação de IAM**: o laboratório bloqueia `iam:CreateRole`. Tudo que precisa de uma role (cluster EKS, node group) reaproveita a **LabRole** fornecida pelo laboratório via data source.
- **Sem IRSA**: criar um provedor OIDC também é bloqueado, então os Pods herdam credenciais AWS do **instance profile do node**. Os serviços já usam a cadeia de credenciais padrão do SDK quando `fiapx.aws.endpoint-override` está em branco, então nenhuma chave estática chega a qualquer container.
- **Credenciais de sessão rotacionam** a cada sessão de laboratório de ~4h (access key, secret e session token). Exporte credenciais novas antes de cada execução de terraform/kubectl.
- **Região**: apenas `us-east-1`.
- **SES em sandbox**: remetente **e** destinatários precisam ser identidades de e-mail verificadas. O Terraform cria as identidades; cada caixa de e-mail ainda precisa clicar no link de confirmação enviado pelo SES.
- **Uma única instância RDS** hospeda os quatro bancos lógicos (`auth_db`, `video_db`, `processing_db`, `notification_db`) por motivo de orçamento — database-per-service (ADR-004) é mantido no nível lógico (veja [ADR-014](../docs/ADR/ADR-014-shared-rds-instance.md)).

## 📋 Runbook de provisionamento

1. Inicie o Learner Lab e abra **AWS Details → AWS CLI** para pegar as credenciais de sessão.
2. Exporte-as (exemplo em PowerShell; use `export` no bash):

   ```powershell
   $env:AWS_ACCESS_KEY_ID     = "..."
   $env:AWS_SECRET_ACCESS_KEY = "..."
   $env:AWS_SESSION_TOKEN     = "..."
   $env:AWS_DEFAULT_REGION    = "us-east-1"
   ```

3. Provisione:

   ```sh
   cd infrastructure/terraform
   cp terraform.tfvars.example terraform.tfvars   # preencha os e-mails do SES
   terraform init
   terraform plan
   terraform apply
   ```

4. Leia os outputs que alimentam os values do Helm e o workflow de CD:

   ```sh
   terraform output                     # tudo que não é sensível
   terraform output -raw rds_password   # senha do DB para o secret do K8s
   ```

5. Configure o kubectl:

   ```sh
   aws eks update-kubeconfig --name fiapx-eks --region us-east-1
   ```

O state do Terraform permanece **local** (`terraform.tfstate`, ignorado pelo Git): o laboratório é efêmero e um backend de state remoto adicionaria configuração sem benefício aqui. Mantenha o arquivo de state entre sessões — a conta do laboratório preserva seus recursos quando uma sessão termina (só as credenciais expiram), então o mesmo state suporta applies incrementais e `terraform destroy`.

## 🔀 Topologia das filas (espelha `localstack/init-aws.sh`)

| Tópico | → Fila | Consumidor |
|---|---|---|
| `video-uploaded` | `video-processing-queue` | processing-worker |
| `video-processed` | `video-results-queue` | video-service |
| `video-processed` | `notification-queue` | notification-service |
| `video-failed` | `video-results-queue` | video-service |
| `video-failed` | `notification-queue` | notification-service |

Toda fila tem uma dead-letter queue `<name>-dlq` (`maxReceiveCount: 5`), e todas as subscriptions usam raw message delivery. A `video-processing-queue` usa um **visibility timeout de 15 minutos**: os jobs de ffmpeg rodam até 10 minutos, e com múltiplas réplicas do worker (competing consumers) um timeout menor causaria reentrega de mensagens em processamento para outras réplicas — duplicando trabalho e empurrando jobs legitimamente lentos para a DLQ.

## 🔎 Documentação relacionada

- [`infrastructure/helm/README.md`](helm/README.md) - deploy dos microsserviços no Kubernetes
- [`docs/HLD/10-deployment-architecture.md`](../docs/HLD/10-deployment-architecture.md) - visão arquitetural de deploy
- [`docs/ADR/`](../docs/ADR/README.md) - decisões arquiteturais (ADR-004, ADR-005, ADR-014, ADR-016)
