# 🛟 Troubleshooting

Problemas reais encontrados durante o desenvolvimento e a validação da plataforma, e como diagnosticá-los/resolvê-los. Organizado por área.

## ☁️ AWS Academy / Sessão de laboratório

### `AccessDenied` em qualquer chamada AWS (CLI local ou pods)

**Sintoma:** `aws eks describe-cluster`, `aws s3 ls`, `aws sqs get-queue-attributes` etc. retornam `AccessDenied`, geralmente citando uma policy `voc-cancel-cred`.

**Causa:** a sessão do AWS Academy Learner Lab expirou (~4h) ou foi encerrada. `sts:GetCallerIdentity` continua funcionando mesmo assim (não é bloqueado pela policy de deny), o que confunde o diagnóstico.

**Diagnóstico rápido:** rode `aws sts get-caller-identity` (deve funcionar) e depois `aws eks describe-cluster --name fiapx-eks --region us-east-1 --query cluster.status` (vai falhar se a sessão morreu). O cluster Kubernetes em si continua respondendo (o `kubectl` usa um token em cache, independente da policy de deny da AWS), então os pods continuam rodando - só chamadas *novas* à API da AWS falham.

**Solução:** iniciar uma nova sessão no AWS Academy, copiar as credenciais novas (`AWS Details > Show > AWS CLI`) para `~/.aws/credentials` e atualizar os secrets do GitHub Actions (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`) via `gh secret set`.

### Upload de vídeo trava em `PROCESSING` para sempre

**Causa mais provável:** a sessão AWS expirou *durante* o processamento - o `processing-worker` perdeu a capacidade de acessar S3/SQS no meio do fluxo (ver IMDS abaixo). Confirme testando uma chamada de API AWS qualquer; se falhar, é isso.

## 🔑 Credenciais AWS dentro dos pods (IMDS)

### Todo pod falha silenciosamente em qualquer chamada S3/SQS/SNS

**Causa:** os node groups gerenciados do EKS criam o launch template com `HttpPutResponseHopLimit = 1` por padrão. Um pod fica em um network namespace separado do host, então uma chamada ao Instance Metadata Service (IMDS, `169.254.169.254`) a partir de um pod precisa de **2 hops** (netns do pod → netns do host → IMDS); com o limite em 1, o pacote é descartado e o pod nunca consegue credenciais AWS via `LabRole`. Processos rodando direto no host (1 hop) funcionam normalmente - por isso o sintoma só aparece dentro da aplicação, nunca em um teste manual no node.

**Diagnóstico:** `aws ec2 describe-instances --query "...MetadataOptions..."` mostrando `HopLimit: 1`.

**Solução definitiva:** `infrastructure/terraform/eks.tf` já define um `aws_launch_template` dedicado com `http_put_response_hop_limit = 2`, referenciado por `aws_eks_node_group.default`. Qualquer novo node group deve usar esse mesmo launch template - não corrigir só via `aws ec2 modify-instance-metadata-options` manual, porque isso não sobrevive a uma recriação do node group (nova sessão do lab, scale event, etc.).

## 🔐 Erros de autenticação confusos

### `401 Missing or invalid token` mesmo com um token válido

**Causa:** `JwtAuthenticationFilter` estende `OncePerRequestFilter`, cujo comportamento padrão (`shouldNotFilterErrorDispatch() == true`) pula o filtro no forward interno do Spring Boot para `/error` após qualquer exceção não tratada (um 500 de verdade, por exemplo). O request refeito então cai em `anyRequest().authenticated()` sem nunca reautenticar, e o cliente recebe um 401 genérico em vez do erro real - mascarando qualquer problema real (o caso concreto encontrado foi o bug de IMDS acima).

**Diagnóstico:** compare uma chamada `GET` (só lê do banco) com uma `POST` que toca AWS (S3/SQS) usando o mesmo token - se o `GET` funciona e o `POST` retorna 401, o problema não é o token.

**Solução:** `JwtAuthenticationFilter` (em `identity-service` e `video-service`) sobrescreve `shouldNotFilterErrorDispatch()` para retornar `false`, garantindo que o erro real apareça.

## 🧪 CI / Smoke tests

### `ffmpeg: command not found` no runner do GitHub Actions

O runner `ubuntu-latest` não vem com `ffmpeg` pré-instalado (contrário ao que se assume às vezes). `cd.yml` instala explicitamente com `apt-get install -y ffmpeg` antes do smoke test.

### Smoke test falha no registro/upload

- `/api/auth/register` exige o campo `name` (`@NotBlank`) - um payload só com `email`/`password` falha.
- A resposta do upload (`POST /api/videos`) usa o campo `videoId`, não `id` (diferente do `GET`, que usa `id` - inconsistência conhecida da API, documentada mas não corrigida por estar fora do escopo do bugfix original).

### `gradle sonar` aponta para `localhost:9000` em vez do SonarCloud

Sem um `sonar.host.url` explícito, o plugin do Gradle usa um SonarQube local por padrão. `sonar.host.url` é fixado direto em cada `build.gradle.kts` (não passado via `-D` pela pipeline), porque é uma escolha arquitetural fixa (SonarCloud), não um segredo que varia por ambiente.

## 🐳 Docker Compose local (PostgreSQL / LocalStack)

Ver a seção "Troubleshooting" de [`docs/setup/local-development.md`](setup/local-development.md) para: porta já em uso, container unhealthy, e como resetar o estado local (`docker compose down -v`).

## 📊 New Relic

### Não sei confirmar se os dados estão chegando

Duas opções sem precisar montar um dashboard manualmente:

1. **All entities** (menu lateral) → deve listar os 4 serviços (`identity-service`, `video-service`, `processing-worker`, `notification-service`).
2. **Query your data**, rodando: `SELECT count(*) FROM Span WHERE service.name = 'video-service' SINCE 30 minutes ago TIMESERIES`.

O dashboard oficial do projeto é gerenciado como código (`infrastructure/terraform/observability.tf`, `terraform output new_relic_dashboard_url`).
