# ☸️ Helm - Plataforma FIAP X de Processamento de Vídeo

Faz o deploy dos 4 microsserviços no cluster EKS provisionado pelo `infrastructure/terraform`. Veja a [ADR-005](../../docs/ADR/ADR-005-kubernetes.md) (Kubernetes) e a [ADR-014](../../docs/ADR/ADR-014-shared-rds-instance.md) (RDS compartilhado) para as decisões arquiteturais por trás deste layout.

## 📚 Sumário

- [Estrutura dos charts](#-estrutura-dos-charts)
- [Decisões arquiteturais desta fase](#-decisões-arquiteturais-desta-fase)
- [Pré-requisitos](#-pré-requisitos)
- [Ordem de deploy](#-ordem-de-deploy)
- [CD (GitHub Actions)](#-cd-github-actions)

---

## 📦 Estrutura dos charts

```
infrastructure/helm/
├── microservice/     # 1 chart genérico, instalado 4 vezes (1 release por serviço)
├── cluster-setup/    # 1 release: namespace, Secrets compartilhados, Job de bootstrap do DB
└── render-aws-values.sh
```

Cada serviço é implantado como uma **release Helm independente** do mesmo chart `microservice`, respeitando o ciclo de deploy independente exigido pela HLD-14 (CI/CD) - `helm upgrade identity-service` nunca toca nos outros 3 serviços.

## 🏗️ Decisões arquiteturais desta fase

- **Sem Ingress Controller.** O AWS Load Balancer Controller precisa de IRSA (um provedor OIDC do EKS + uma IAM role customizada), ambos bloqueados no AWS Academy (`infrastructure/terraform/iam.tf`). `identity-service` e `video-service` usam, em vez disso, um `Service type: LoadBalancer` puro do Kubernetes, que o cloud provider in-tree do EKS deve satisfazer com um ELB clássico usando apenas a `LabRole` do node - sem controller adicional para instalar. É um desvio documentado do componente "Ingress Controller" listado em `docs/HLD/10-deployment-architecture.md`. Não validado contra um cluster real nesta sessão (sem cluster disponível aqui) - se a `LabRole` não tiver as permissões de ELB, é o primeiro ponto a checar ao validar o primeiro deploy real.
- **Sem IRSA / sem anotações de ServiceAccount.** Os Pods recebem credenciais AWS do instance profile do node EKS (a mesma `LabRole`), exatamente como os serviços já assumem quando `fiapx.aws.endpoint-override` está em branco (cadeia de credenciais padrão do SDK). Nenhuma chave AWS estática em lugar nenhum.
- **processing-worker e notification-service não têm Service.** Nenhum dos dois tem servidor HTTP embutido (`docs/LLD/processing-worker.md`, `docs/LLD/notification-service.md`) - só fazem polling no SQS. A liveness usa o mesmo probe exec `pgrep -f app.jar` do `docker-compose.yml`; não há readiness probe porque nada roteia tráfego para eles.
- **HPA baseado em CPU**, conforme a estratégia inicial da ADR-005/HLD-13 (escalonamento por profundidade de fila via KEDA é uma evolução futura documentada, não implementada aqui).

## ✅ Pré-requisitos

1. `terraform apply` já rodou (`infrastructure/terraform`) e o cluster EKS existe.
2. Aponte `kubectl`/`helm` para o cluster:
   ```bash
   aws eks update-kubeconfig --name "$(terraform -chdir=infrastructure/terraform output -raw eks_cluster_name)"
   ```
3. **metrics-server** - não vem por padrão no EKS, mas é necessário para os HPAs calcularem um alvo de CPU (sem ele reportam `<unknown>` e nunca escalam). Instale o manifesto upstream uma vez por cluster:
   ```bash
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
   ```
4. Imagens publicadas no ECR para os 4 serviços. Dispare o job `build-push-ecr` de `.github/workflows/cd.yml` (`gh workflow run cd.yml`) - ele precisa de 3 secrets do repositório atualizados a partir da sessão ativa do AWS Academy (as credenciais rotacionam a cada ~4h): `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
5. Opcionalmente, `NEW_RELIC_LICENSE_KEY` como secret do repositório (não rotaciona como os 3 acima). Quando presente, o passo "Sync New Relic license key" do job `deploy` aplica o valor direto no Secret Kubernetes `fiapx-newrelic-license` a cada disparo, antes do rollout dos microsserviços - mantendo a observabilidade ([ADR-015](../../docs/ADR/ADR-015-observability-implementation.md)) ativa sem precisar rodar `helm upgrade cluster-setup` manualmente. Quando ausente, esse passo é pulado e os Pods mantêm o valor que já estiver no Secret (veja o comentário do passo em `cd.yml` sobre a responsabilidade compartilhada entre Helm/kubectl).

## 🚀 Ordem de deploy

### 1. Gerar os values do overlay AWS

```bash
cd infrastructure/helm
./render-aws-values.sh
```

Lê o `terraform output` (endpoint do RDS, bucket S3, remetente SES, URLs dos repositórios ECR, região) e gera `infrastructure/helm/generated/*.aws.yaml` (ignorado pelo Git - contém a senha master do RDS e um segredo JWT gerado). Requer `terraform`, `jq` e `openssl` no PATH.

### 2. cluster-setup (namespace, secrets, bootstrap do DB)

```bash
helm upgrade --install cluster-setup ./cluster-setup \
  -f generated/values-cluster-setup.aws.yaml

kubectl wait --for=condition=complete --timeout=120s job/fiapx-db-bootstrap -n fiapx
```

Cria o namespace `fiapx`, os Secrets `fiapx-db-credentials` e `fiapx-jwt-secret`, e roda um Job que cria os 4 bancos lógicos (`auth_db`, `video_db`, `processing_db`, `notification_db`) na instância RDS compartilhada ([ADR-014](../../docs/ADR/ADR-014-shared-rds-instance.md)), caso ainda não existam - reaproveitando a mesma lógica idempotente de `infrastructure/docker/postgres/init-databases.sh`. **Isso precisa terminar antes do passo 3**: a migração Flyway de cada serviço precisa que seu banco já exista.

### 3. As 4 releases de serviço

```bash
cd microservice

helm upgrade --install identity-service . -n fiapx --create-namespace \
  -f values-identity-service.yaml -f ../generated/values-identity-service.aws.yaml

helm upgrade --install video-service . -n fiapx \
  -f values-video-service.yaml -f ../generated/values-video-service.aws.yaml

helm upgrade --install processing-worker . -n fiapx \
  -f values-processing-worker.yaml -f ../generated/values-processing-worker.aws.yaml

helm upgrade --install notification-service . -n fiapx \
  -f values-notification-service.yaml -f ../generated/values-notification-service.aws.yaml
```

Cada par de `-f` precisa ser passado **nessa ordem**: o Helm faz merge de mapas, mas substitui listas inteiras entre arquivos `-f`, e `env` só é declarado no overlay gerado por esse motivo (veja os comentários em cada `values-<service>.yaml`).

### 📍 Encontrar os endpoints públicos

```bash
kubectl get svc identity-service video-service -n fiapx
```

## 🔁 CD (GitHub Actions)

`.github/workflows/cd.yml` é `workflow_dispatch`-only: as credenciais da sessão AWS Academy rotacionam a cada ~4h, então não há identidade AWS de longa duração para disparar isso em todo push. Tem 2 jobs:

- **`build-push-ecr`**: builda o jar de cada serviço e publica a imagem no ECR, marcada com a SHA do commit e `latest`.
- **`deploy`**: resolve o ambiente via consultas diretas à AWS CLI (nomes determinísticos que o Terraform atribui - sem acesso a `terraform output` a partir do CI, já que o state é local à máquina do operador), depois faz `helm upgrade --install` das 4 releases de microsserviço (marcadas pela SHA do commit, não `latest` - para rollback rastreável), e roda um smoke test end-to-end (registro → login → upload de um vídeo real pequeno via `ffmpeg` → poll de status → valida `PROCESSED` → download). **Não** faz deploy do `cluster-setup` - veja os passos manuais acima, feitos uma vez por ambiente.

### 🔑 Configuração necessária no repositório

- **Secrets** (atualizar a cada sessão de laboratório, antes de disparar): `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
- **Variable**: `SES_SENDER_EMAIL` (o mesmo endereço verificado em `infrastructure/terraform/terraform.tfvars` / SES, ou um placeholder se `ses_manage_identities` estiver desabilitado conforme a [ADR-016](../../docs/ADR/ADR-016-aws-academy-ses-verification-constraint.md)).

### 📋 Runbook por sessão de laboratório

1. Inicie a sessão do AWS Academy Lab; copie as credenciais temporárias.
2. Atualize os 3 secrets do GitHub acima (`gh secret set NOME` ou a UI de Settings do repositório).
3. Se a infraestrutura ainda não estiver de pé: `terraform apply` (`infrastructure/terraform`), depois os passos manuais únicos de Pré-requisitos/Ordem de deploy 1-2 acima.
4. Dispare o pipeline: `gh workflow run cd.yml`.
5. Pegue os endpoints públicos no resumo do job de deploy, ou via `kubectl get svc identity-service video-service -n fiapx`.
6. Entre sessões, EKS/RDS continuam consumindo orçamento do AWS Academy independentemente de haver algo implantado; rode `terraform destroy` se o orçamento for uma preocupação, e `terraform apply` de novo na próxima sessão (~15min para o EKS voltar, depois rode o passo manual de cluster-setup mais uma vez, já que um cluster novo começa vazio).

## 🔎 Documentação relacionada

- [`docs/HLD/10-deployment-architecture.md`](../../docs/HLD/10-deployment-architecture.md) - arquitetura de deploy
- [`docs/ADR/`](../../docs/ADR/README.md) - decisões arquiteturais
- [`infrastructure/README.md`](../README.md) - provisionamento Terraform (AWS)
