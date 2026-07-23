# Configuração da Plataforma

Este documento descreve as plataformas externas utilizadas pela FIAP X Video Processing Platform, sua finalidade e como são usadas ao longo do ciclo de vida do projeto.

Nenhuma infraestrutura em nuvem, pipeline de CI/CD ou código de aplicação é criado por este documento. Ele apenas estabelece a referência de configuração para as plataformas abaixo.

---

## GitHub

**Finalidade:** Controle de versão e colaboração para o projeto.

**Uso:**

- Hospeda o repositório `fiapx-video-processing`.
- Pull requests são o caminho obrigatório para a fusão de mudanças.
- Regras de proteção de branch e de revisão se aplicam ao branch padrão.

---

## Amazon ECR

**Finalidade:** Registro de imagens de container para os microsserviços definidos em `services/`. O GHCR foi a opção originalmente considerada (HLD-14); o Amazon ECR é o que `infrastructure/terraform/ecr.tf` (Epic 008) efetivamente provisiona, um repositório por serviço (`fiapx/<service-name>`).

**Uso:**

- Cada imagem de serviço é construída a partir do seu `Dockerfile` e enviada para `<account-id>.dkr.ecr.us-east-1.amazonaws.com/fiapx/<service-name>`.
- A autenticação usa as credenciais de sessão do AWS Academy configuradas como secrets do repositório (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`) - veja o job `build-push-ecr` de `.github/workflows/cd.yml`.
- A publicação da imagem em si é executada pelo CD (Epic 010), o que está fora do escopo desta tarefa.

---

## SonarCloud

**Finalidade:** Análise estática de código e quality gate, conforme ADR-010.

**Uso:**

- Um projeto SonarCloud é configurado por serviço (ou um projeto cobrindo o monorepo, conforme a convenção da organização).
- Relatórios de cobertura (Jacoco) e resultados de análise estática são enviados usando um token do SonarCloud.
- Os resultados do quality gate são usados como critério de merge assim que o CI/CD for implementado.

---

## New Relic

**Finalidade:** Application Performance Monitoring (APM), logs, métricas e tracing distribuído, conforme ADR-008.

**Uso:**

- Os serviços são instrumentados com OpenTelemetry e exportam telemetria para o New Relic.
- Uma license key do New Relic autoriza a ingestão para a conta.
- O CloudWatch permanece como a fonte de métricas de infraestrutura AWS; o New Relic concentra a observabilidade em nível de aplicação.

---

## AWS Academy Lab

**Finalidade:** Ambiente de validação apenas, conforme ADR-002 e ADR-009.

**Uso:**

- Fornece credenciais AWS temporárias, baseadas em sessão (access key, secret key e session token) que expiram quando a sessão do lab termina.
- Usado exclusivamente para validar o comportamento da aplicação contra serviços AWS reais (S3, SNS, SQS) durante o desenvolvimento.
- Nenhum recurso AWS, Terraform, CloudFormation, política IAM ou perfil da AWS CLI é criado como parte desta tarefa.

---

## Tratamento de Secrets

- Nenhuma credencial real é armazenada neste repositório.
- O `.env.example` documenta os nomes de variáveis necessários usando apenas placeholders.
- Os valores reais são fornecidos localmente (máquina do desenvolvedor) ou por meio do próprio repositório de secrets da plataforma (por exemplo, secrets do GitHub Actions) quando o CI/CD for implementado em uma tarefa futura.
