# 🕑 Decision Log

Registro cronológico de decisões de projeto que não geraram um ADR formal (mudanças de arquitetura consolidada estão nos [ADRs](ADR/README.md)), mas que moldaram como o produto foi entregue. Serve de contexto para quem for dar manutenção depois.

## Ambiente e infraestrutura

- **AWS Academy como ambiente de validação** - credenciais de sessão temporárias (~4h), sem criação de IAM Role própria nem OIDC/IRSA. O cluster EKS e os pods reutilizam a `LabRole` compartilhada via instance profile do node. Restrição aceita do ambiente de laboratório, não um gap a corrigir - ver `docs/HLD/11-security.md`.
- **CD via `workflow_dispatch` manual, não automático em push** - decisão direta consequência do ponto acima: como as credenciais AWS expiram a cada sessão do lab, um deploy automático a cada push falharia de forma imprevisível. O operador dispara o deploy manualmente quando tem uma sessão ativa, após atualizar os secrets do GitHub.
- **1 instância RDS para os 4 bancos lógicos** (ADR-014) - `Database per Service` continua valendo no nível lógico (schemas/bancos separados, sem acesso cruzado), mas fisicamente compartilham uma instância `db.t3.micro`, por restrição de orçamento/permissões do AWS Academy.
- **Concorrência via SQS + réplicas + HPA, não paralelismo interno no consumer** - o `VideoUploadedConsumer` do `processing-worker` processa uma mensagem por vez; a concorrência multi-vídeo vem inteiramente de múltiplas réplicas competindo pela fila (Competing Consumers) e do HPA escalando essas réplicas sob carga. Decisão registrada explicitamente no roadmap de fechamento: paralelismo interno só seria avaliado se os testes de carga demonstrassem gargalo real - o Cenário C (k6) comprovou que o HPA sozinho já resolve (1→3 réplicas sob spike), então a decisão original foi mantida.
- **IMDS hop limit dos node groups EKS: `HttpPutResponseHopLimit = 2`** - o padrão do EKS managed node group é `1`, que bloqueia qualquer pod (2 hops: netns do pod → netns do host → IMDS) de obter credenciais AWS via instance profile - só processos rodando direto no host (1 hop) conseguiam. Como não há IRSA/OIDC neste ambiente, IMDS via `LabRole` é o único caminho de credencial que qualquer pod tem, então esse valor incorreto quebrava silenciosamente toda chamada S3/SQS/SNS de qualquer serviço. Corrigido via `aws_launch_template` dedicado em `infrastructure/terraform/eks.tf` (não só via `aws ec2 modify-instance-metadata-options` manual, que não sobrevive a uma recriação do node group).

## Registro/Container

- **Amazon ECR, não GHCR** - o HLD original considerava GHCR como opção; o Terraform (`infrastructure/terraform/ecr.tf`) provisiona ECR de fato, e o CD usa ECR. Decisão de sincronização (a documentação foi ajustada para refletir o código, não o contrário), não uma mudança de arquitetura nova.

## Observabilidade

- **OpenTelemetry (agente Java, auto-instrumentação) + New Relic (APM) + CloudWatch (infraestrutura)** - Prometheus/Grafana/Micrometer standalone foram avaliados e descartados por redundância frente ao que a New Relic já oferece, dado o escopo do hackathon. Ver ADR-015.
- **Dashboard New Relic como código (Terraform `newrelic_one_dashboard`), não montado manualmente na UI** - versionado em `infrastructure/terraform/observability.tf`, reproduzível e revisável em PR. Widgets usam `span.kind IN ('server', 'consumer')` para cobrir tanto os serviços HTTP (`identity-service`, `video-service`) quanto os serviços orientados a mensageria (`processing-worker`, `notification-service`, cujos spans raiz são do tipo `consumer`, não `server`) - descoberto e corrigido durante a validação com dados reais via NerdGraph antes do fechamento do Epic 016/017.

## Segurança

- **CORS configurável, fechado por padrão** - `identity-service` e `video-service` expõem `IDENTITY_CORS_ALLOWED_ORIGINS`/`VIDEO_CORS_ALLOWED_ORIGINS`; nenhuma origem é permitida até ser explicitamente configurada. Decisão tomada quando o Epic 013 (frontend de demonstração) introduziu a primeira necessidade real de chamada cross-origin.
- **Usuário não-root nos 4 Dockerfiles** e **limite de tamanho de upload configurável** (`VIDEO_UPLOAD_MAX_FILE_SIZE`) - fechados no Epic 015 (Security Hardening); ver `docs/HLD/11-security.md`.

## Qualidade e testes

- **k6 para evidência de carga, sem duplicar com testes de unidade/integração** - 3 cenários (burst, sustentado, spike), cada um mapeado a um conjunto específico de RF/RNF (ver [`rf-rnf-traceability.md`](rf-rnf-traceability.md)). Captura automática de evidência de HPA (`kubectl get hpa`/`top pods`) rodando em paralelo ao cenário C, para provar escala real, não apenas configuração.
- **`application.port` (singular, na ADR-011/LLD originais) corrigido para `application.ports` (plural)** - o código real e a maioria das LLDs já usavam o plural; a ADR-011 foi atualizada para refletir o código, por ser uma sincronização documental, não uma mudança de arquitetura.
