# Fundação de CI/CD

Este documento descreve o pipeline de Integração Contínua que valida cada Pull Request antes do merge, conforme ADR-010.

Nenhum deployment é realizado por este pipeline. CD, deployment em Kubernetes, provisionamento AWS, Terraform e automação de release estão explicitamente fora do escopo e ficam para uma tarefa futura.

---

## Workflow

**Arquivo:** [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml)

**Gatilhos:**

- `pull_request` direcionado a `main`.
- `workflow_dispatch` (execução manual).

**Estratégia:** um único job (`build-test-analyze`) roda como uma matrix sobre os quatro projetos Gradle independentes em `services/`:

- `identity-service`
- `notification-service`
- `processing-worker`
- `video-service`

Cada execução da matrix é independente (`fail-fast: false`), atendendo ao requisito do ADR-010 de que cada serviço tenha um build independente.

### Passos por serviço

1. **Checkout** do repositório.
2. **Configuração do Java 21 (Temurin)** e do **Gradle 8.8**, correspondendo ao toolchain declarado em cada `build.gradle.kts` e `gradle-wrapper.properties`.
3. **Resolução do comando Gradle** — usa `./gradlew` quando um serviço possui um wrapper commitado, caso contrário recorre ao `gradle` instalado no sistema (veja a decisão abaixo).
4. **Build e execução dos testes unitários** — task `build` (executa `test` como dependência).
5. **Geração do relatório de cobertura JaCoCo** — `jacocoTestReport`, produzindo `build/reports/jacoco/test/jacocoTestReport.xml`. O relatório é enviado como artefato do workflow independentemente do resultado do build.
6. **Análise do SonarCloud** — task `sonar`, apenas quando `SONAR_TOKEN` está configurado (veja abaixo). Isso mantém o workflow utilizável antes de o SonarCloud estar conectado, e evita falhar PRs em ambientes onde o secret não está disponível.
7. **Build da imagem Docker** — reutiliza o `Dockerfile` já existente de cada serviço, com a tag `fiapx/<service>:ci`. A imagem é construída localmente apenas no runner; ela nunca é enviada (push) para nenhum registry (a publicação no ECR é responsabilidade do CD, fora do escopo aqui).
8. **Scan de vulnerabilidades com Trivy (preparação)** — escaneia a imagem recém-construída e envia os resultados SARIF para a aba Security do repositório. `exit-code: '0'` significa que o scan nunca falha o build; isto é *preparação* do scan, não um gate obrigatório. Transformá-lo em um gate bloqueante é uma decisão futura.

---

## Decisões Arquiteturais

### A URL do host do Sonar é a única configuração do Sonar que reside no `build.gradle.kts`

`jacoco` e `org.sonarqube` são aplicados no `build.gradle.kts` de cada serviço. Valores específicos de ambiente — token, organization, project key — continuam sendo passados pelo workflow como system properties `-D`, não fixados no código, conforme a consequência do ADR-002 de que "a configuração deve ser externa ao código."

`sonar.host.url` é a única exceção, e agora é definido diretamente em cada `build.gradle.kts`:

```kotlin
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
```

Sem um `sonar.host.url` explícito, o plugin Gradle Sonar assume por padrão um servidor SonarQube local (`http://localhost:9000`). Esse padrão está errado para este projeto: o ADR-010 compromete a plataforma com o **SonarQube Cloud**, não um servidor SonarQube autogerenciado — esta é uma escolha arquitetural fixa, não um secret por ambiente ou um valor que muda entre máquinas de desenvolvedores, CI ou organizações do SonarCloud. Tratá-lo como os outros três valores (uma flag `-D` fornecida apenas pelo `ci.yml`) fazia com que `gradle sonar` apontasse silenciosamente para um servidor local por padrão para qualquer pessoa executando fora do CI — o oposto do comportamento pretendido. Fixá-lo no arquivo de build torna o SonarCloud o padrão em todo lugar onde a task `sonar` roda, enquanto token/organization/project key continuam vindo do ambiente porque esses valores *de fato* variam.

**Validado:** executar `gradle sonar` localmente (sem flags `-D`, sem token) agora falha com `You must define the following mandatory properties for '<service>': sonar.organization` em vez de tentar acessar `localhost:9000`. `sonar.organization` é um requisito exclusivo do SonarCloud imposto pelo plugin — o plugin Gradle Sonar não o exige ao apontar para um servidor SonarQube autogerenciado — então esse erro é prova direta de que o plugin agora resolve `sonar.host.url` para o SonarCloud por padrão, nos quatro serviços.

O passo `sonar` do `ci.yml` não passa mais `-Dsonar.host.url` (removido por ser agora uma duplicata redundante do padrão do arquivo de build); ele ainda passa `-Dsonar.token`, `-Dsonar.organization` e `-Dsonar.projectKey`, que permanecem como secrets/variáveis fornecidos pelo CI.

### Um projeto SonarCloud por serviço

Dado que cada diretório em `services/*` é um projeto Gradle construído de forma independente (com seu próprio `settings.gradle.kts`, e seu próprio `gradlew` quando presente) em vez de um build Gradle multi-módulo, cada serviço é analisado como seu próprio projeto SonarCloud em vez de ser mesclado em uma análise combinada única. A project key é derivada por convenção:

```
<SONAR_PROJECT_KEY>-<service-name>
```

por exemplo, com `SONAR_PROJECT_KEY=fiapx-video-processing`, o Video Service é analisado sob `fiapx-video-processing-video-service`. Todos os serviços compartilham a mesma `SONAR_ORGANIZATION`.

### Preferir `./gradlew`, com fallback para um Gradle de sistema fixado

Cada passo resolve `./gradlew` quando presente e só recorre a um `gradle` instalado no sistema como fallback. O `video-service` atualmente não possui seu launcher `gradlew`/`gradlew.bat` e o jar do wrapper — uma lacuna pré-existente que o `ci-scaffold.yml` já preenchia gerando e commitando wrappers a cada push. Em vez de depender daquela automação separada ter sido executada, `gradle/actions/setup-gradle` instala o Gradle `8.8` como fallback, correspondendo à versão agora fixada no `gradle-wrapper.properties` de cada serviço (veja abaixo), de forma que o pipeline funciona identicamente com ou sem um wrapper presente.

### Versão do Gradle wrapper corrigida de 9.5.1 para 8.8

Validar o pipeline localmente (`./gradlew build`) revelou um defeito real e pré-existente: o `gradle-wrapper.properties` dos quatro serviços estava fixado no Gradle `9.5.1`, que é incompatível com o plugin Gradle do Spring Boot `3.3.2` já declarado em todo `build.gradle.kts` — a task `bootJar` falha completamente (`CopyProcessingSpec.getDirMode()`, uma API que o plugin do Spring Boot ainda espera da linha Gradle 8.x). O `ci-scaffold.yml` já declarava a versão de Gradle de CI pretendida pelo projeto como `8.4`, confirmando que `9.5.1` era uma incompatibilidade e não um upgrade intencional. O `gradle-wrapper.properties` dos quatro serviços foi atualizado para o Gradle `8.8` — o release 8.x mais recente, totalmente compatível tanto com o Spring Boot `3.3.2` quanto com o JDK 21 — e o fallback do `ci.yml` foi alinhado com a mesma versão. Isso foi verificado executando `build` e `jacocoTestReport` localmente para os quatro serviços após a mudança (veja Resultados da Validação).

### Plugin `io.spring.dependency-management` ausente adicionado ao `identity-service` e ao `video-service`

`notification-service` e `processing-worker` já declaravam `id("io.spring.dependency-management") version "1.1.0"`; `identity-service` e `video-service` não. Sem ele, o BOM do Spring Boot nunca é importado, então as dependências sem versão `implementation("org.springframework.boot:...")` nesses dois serviços falham ao resolver (`Could not find org.springframework.boot:spring-boot-starter-web:.`, versão vazia). Isso foi reproduzido localmente e corrigido adicionando a mesma declaração de plugin já usada pelos outros dois serviços — uma correção de uma linha, inequívoca, necessária para que `compileJava` sequer tenha sucesso.

### Correção no `.gitignore`: `.github/` estava sendo silenciosamente ignorado

A regra genérica `.*` adicionada ao `.gitignore` em uma tarefa anterior (para ocultar `.ai`) também correspondia a `.github`, já que ele começa com ponto — significando que os novos arquivos `ci.yml`, `PULL_REQUEST_TEMPLATE.md` e `CODEOWNERS` eram invisíveis para `git status` e nunca poderiam ser commitados. Corrigido adicionando `!.github/` após a regra genérica, o mesmo padrão de negação já usado para `.env.example`.

### `ci-scaffold.yml` removido (TASK-002.6)

`.github/workflows/ci-scaffold.yml` era um resquício do passo de scaffolding da TASK-002.1: ele regenerava os wrappers do Gradle a cada push para `services/**` usando o Gradle 8.4 (inconsistente com o 8.8 fixado em todo o restante) por meio de uma action obsoleta, e então committava e enviava (push) o resultado automaticamente com `contents: write`. Ele nunca foi um gate de Pull Request e nunca foi referenciado por nenhuma outra documentação. Seu único propósito real — manter o Gradle wrapper presente e reproduzível — agora é tratado diretamente ao commitar o jar do wrapper e o script `gradlew` de cada serviço (veja a correção de reprodutibilidade do wrapper abaixo), então o workflow foi removido por ser redundante e arriscado (um workflow de auto-push competindo com o trabalho real de PR).

---

## Defeitos Pré-Existentes Conhecidos Revelados por Este Pipeline (Não Corrigidos)

Executar o pipeline localmente contra uma instância real do PostgreSQL 16 (a provisionada na TASK-002.3) revelou dois defeitos pré-existentes em nível de aplicação, deliberadamente **não** corrigidos aqui — eles estão fora do escopo de "fundação de CI/CD" e requerem uma decisão em nível de aplicação, não de ferramental de build:

- **O teste do `video-service` falha contra o PostgreSQL real** — seu `ApplicationUnitTest` não possui `@ActiveProfiles("test")` (diferente do `identity-service`, que possui e usa um perfil de teste H2 em memória), então ele carrega o datasource padrão do `application.yml` e atinge uma conexão real Flyway/PostgreSQL 16. Ele falha com `FlywayException: Unsupported Database: PostgreSQL 16.14`, o que muito provavelmente significa que o módulo `org.flywaydb:flyway-database-postgresql` (exigido pelo Flyway 10.x para suporte ao PostgreSQL) está ausente nas dependências do `video-service`.
- **O `notification-service` está sem o driver JDBC do PostgreSQL** — ele configura um `spring.datasource.url` e habilita o Flyway no `application.yml`, mas seu `build.gradle.kts` nunca declara `org.postgresql:postgresql`. Seu teste atualmente passa apenas porque, sem um driver, o Spring Boot não consegue construir um bean `DataSource`, então a auto-configuração do Flyway silenciosamente nunca é ativada — o serviço não consegue de fato se conectar ao seu banco de dados em um deployment real.

Recomenda-se uma tarefa de acompanhamento para decidir a estratégia de banco de dados de teste (H2 vs. Testcontainers vs. Postgres real) e para fechar as lacunas de dependências ausentes.

---

## Secrets Obrigatórios do GitHub

Configurados nas configurações do repositório GitHub (`Settings > Secrets and variables > Actions`), não commitados no repositório. Os nomes correspondem ao [`.env.example`](../../.env.example) de `platform-setup.md`:

| Secret | Finalidade |
|---|---|
| `SONAR_TOKEN` | Autentica a análise do SonarCloud. Quando ausente, o passo de análise é ignorado em vez de falhar o pipeline. |
| `SONAR_ORGANIZATION` | Chave de organização do SonarCloud compartilhada por todos os quatro projetos de serviço. |
| `SONAR_PROJECT_KEY` | Chave de projeto base; combinada com o nome do serviço por serviço (veja acima). |

`GITHUB_TOKEN` é fornecido automaticamente pelo GitHub Actions e não requer configuração manual.

Nenhum secret de AWS, ECR ou New Relic é necessário para este workflow — nenhum de seus passos os utiliza.

---

## Template de Pull Request e CODEOWNERS

- [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md) — campos de descrição, motivação, impacto e evidência de teste, correspondendo ao checklist de PR em `.ai/rules/git-rules.md`.
- [`.github/CODEOWNERS`](../../.github/CODEOWNERS) — atualmente atribui `@jreigeVic` como o owner padrão para todo o repositório. Atualize este arquivo conforme a equipe cresce.

---

## Passos Manuais

1. Criar uma organização SonarCloud e um projeto por serviço (ou confirmar que a convenção de chave acima corresponde à sua configuração do SonarCloud).
2. Adicionar `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` como secrets do repositório.
3. Habilitar proteção de branch em `main` exigindo que os checks da matrix `build-test-analyze` (e a revisão do CODEOWNERS) passem antes do merge.
4. Revisar `.github/CODEOWNERS` e adicionar owners adicionais conforme a equipe cresce.

---

## Resultados da Validação

- **Sintaxe do workflow**: `.github/workflows/ci.yml` passa no `actionlint` (via a imagem Docker `rhysd/actionlint`) sem nenhum achado, incluindo a validação de expressão/contexto (um `if: ${{ secrets.SONAR_TOKEN != '' }}` inicial foi identificado e corrigido — `secrets` não é um contexto válido em `if:` no nível de passo). O `ci-scaffold.yml` foi removido na TASK-002.6 (veja acima) e não faz mais parte do pipeline.
- **Build**: `./gradlew build` verificado localmente, após as correções, para os quatro serviços:
  - `identity-service`: `BUILD SUCCESSFUL`.
  - `video-service`: build/compile/`bootJar` têm sucesso; a task `test` falha (veja Defeitos Pré-Existentes Conhecidos).
  - `notification-service`: `BUILD SUCCESSFUL`.
  - `processing-worker`: `BUILD SUCCESSFUL`.
- **JaCoCo**: `jacocoTestReport.xml` gerado e verificado (não vazio, bem formado) para `identity-service`, `notification-service`, `processing-worker`.
- **Configuração do SonarCloud**: plugin `org.sonarqube` aplicado nos quatro `build.gradle.kts`; passo do workflow verificado para ser ignorado de forma limpa (exit 0) em vez de falhar quando `SONAR_TOKEN` está ausente, já que ainda não existe nenhum projeto SonarCloud para este repositório.
- **Direcionamento de host do SonarCloud**: `gradle sonar` executado localmente sem flags `-D` para os quatro serviços (`video-service` via `gradle -p ../video-service sonar` a partir do wrapper do `identity-service`, já que seu próprio wrapper foi deliberadamente deixado intocado). Todos os serviços falharam identicamente com `You must define the following mandatory properties for '<service>': sonar.organization` — a validação exclusiva do SonarCloud que o plugin realiza, provando que `sonar.host.url` agora resolve para o SonarCloud por padrão em vez de `localhost:9000`.
- **Preparação do scan de segurança**: passo do Trivy configurado com `exit-code: '0'` (não bloqueante) e envio de SARIF para a aba Security; não executado ponta a ponta localmente já que depende do passo anterior de build da imagem Docker rodando dentro do Actions.

---

## Fora do Escopo

- Continuous Deployment.
- Deployment em Kubernetes.
- Provisionamento de infraestrutura AWS.
- Terraform.
- Automação de release.
- Envio (push) de imagens para um registry (aqui é apenas build, para fins de scan; o push para o ECR é responsabilidade do CD - `.github/workflows/cd.yml`).
