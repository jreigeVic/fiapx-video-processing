# 🤝 Contribuindo

Obrigado por contribuir com a FIAP X Video Processing Platform. Este arquivo é um ponto de entrada curto — as regras detalhadas vivem nos documentos linkados.

## 1. Configure seu ambiente local

- Contas e credenciais de plataforma (somente placeholders): [`docs/setup/platform-setup.md`](docs/setup/platform-setup.md)
- Infraestrutura local (PostgreSQL, LocalStack via Docker Compose): [`docs/setup/local-development.md`](docs/setup/local-development.md)
- Visão geral do pipeline de CI: [`docs/setup/ci-cd.md`](docs/setup/ci-cd.md)

## 2. Siga o fluxo de engenharia

Toda mudança passa por: **Design → Revisão de Arquitetura → Implementação → Validação → Revisão de Código → Commit**.

Detalhes completos, nomenclatura de branches, convenções de commit, processo de Pull Request, política de code review, expectativas de teste, e como o desenvolvimento assistido por IA se encaixa: [`docs/development/workflow.md`](docs/development/workflow.md).

## 3. Saiba quando terminou

Antes de abrir um PR, confira sua mudança contra o [`docs/development/definition-of-done.md`](docs/development/definition-of-done.md).

## 4. Trabalhando com IA

Este repositório segue um fluxo de engenharia AI-first. Se você está usando Claude Code ou um assistente similar, ele deve ler o `CLAUDE.md` primeiro — que define o contrato operacional (tarefa → contexto → plano → aprovação → implementação → testes → resumo) seguido pelas mudanças assistidas por IA neste repositório. O `CLAUDE.md` hoje vive um diretório acima deste repositório, junto do repositório complementar `ai_os`, em vez de dentro do `fiapx-video-processing`.

## 5. Abra um Pull Request

- Use nomes de branch seguindo `feature/*`, `hotfix/*` ou `docs/*`, a partir de `main`.
- Preencha o template de PR ([`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md)) — ele é aplicado automaticamente.
- Os revisores são determinados pelo [`.github/CODEOWNERS`](.github/CODEOWNERS).
- O CI (`.github/workflows/ci.yml`) precisa passar antes do merge.

Nunca commite credenciais reais — veja a seção de Tratamento de Secrets em `docs/setup/platform-setup.md`.
