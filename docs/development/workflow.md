# Fluxo de Desenvolvimento

Este documento padroniza como o desenvolvimento e realizado neste repositorio, para que qualquer contribuidor - humano ou assistido por IA - trabalhe de forma consistente. Nao cobre codigo de aplicacao, infraestrutura, Docker, Kubernetes, AWS ou implementacao de CI/CD; ver [`docs/setup/`](../setup/) para isso.

---

## Fluxo de Engenharia

Toda mudanca - escrita por um humano, por IA, ou por ambos - segue as mesmas seis etapas:

```
Design
  ↓
Revisao de Arquitetura
  ↓
Implementacao
  ↓
Validacao
  ↓
Revisao de Codigo
  ↓
Commit
```

1. **Design** - Entender a tarefa, ler o contexto relevante (`CLAUDE.md`, a descricao da tarefa, as ADRs/HLD/LLD existentes, e apenas os arquivos de codigo necessarios), e apresentar um plano de implementacao conciso antes de escrever codigo.
2. **Revisao de Arquitetura** - Confirmar que o plano respeita a arquitetura existente (Clean/Hexagonal Architecture, DDD, Event-Driven Architecture, e as ADRs em `docs/ADR/`). Qualquer mudanca arquitetural exige uma ADR aprovada - nunca alterar a arquitetura consolidada silenciosamente. Aguardar aprovacao explicita antes de prosseguir quando a mudanca envolver arquitetura.
3. **Implementacao** - Implementar apenas o que a tarefa exige. Preferir modificar codigo existente a criar novos arquivos. Seguir as convencoes existentes do projeto.
4. **Validacao** - Compilar, rodar testes unitarios (e de integracao quando aplicavel), gerar cobertura do JaCoCo, e corrigir falhas. Ver [Expectativas de Teste](#expectativas-de-teste) abaixo.
5. **Revisao de Codigo** - Toda mudanca e revisada antes do merge. Ver [Processo de Revisao de Codigo](#processo-de-revisao-de-codigo) abaixo.
6. **Commit** - Commitar com uma mensagem no padrao Conventional Commit (ver abaixo). Nenhum segredo commitado. Documentacao atualizada quando necessario (ver [Definicao de Pronto](./definition-of-done.md)).

### Desenvolvimento assistido por IA

Este repositorio segue um fluxo de engenharia AI-first: as decisoes arquiteturais sao tomadas pelos engenheiros, e a implementacao repetitiva e acelerada por IA. Quando a IA auxilia em qualquer uma das seis etapas acima, ela segue:

- **`CLAUDE.md`** (vive um diretorio acima deste repositorio, junto do repositorio complementar `ai_os`) - o contrato operacional para trabalho assistido por IA neste repositorio: ler a tarefa, ler apenas os arquivos necessarios, analisar, apresentar um plano, aguardar aprovacao antes de mudancas arquiteturais, implementar, rodar testes e corrigir falhas, resumir. Isso mapeia diretamente para as etapas Design → Revisao de Arquitetura → Implementacao → Validacao acima.
- **`ai_os/.ai/rules/ai-workflow-rules.md`** (repositorio complementar AI OS) - a politica mais ampla de fluxo de trabalho com IA: consultar `.ai/context` e `.ai/rules` antes de gerar codigo, nunca assumir requisitos que nao existem, perguntar quando houver ambiguidade, nunca alterar a arquitetura consolidada sem uma ADR, e manter a documentacao sincronizada.

Mudancas assistidas por IA sao submetidas a mesma Revisao de Codigo e Definicao de Pronto que qualquer outra mudanca - a participacao da IA nao dispensa a revisao.

---

## Estrategia de Branches

Conforme `ai_os/.ai/rules/git-rules.md`:

| Padrao de branch | Finalidade |
|---|---|
| `main` | Sempre estavel. |
| `feature/*` | Novas funcionalidades. |
| `hotfix/*` | Correcoes criticas. |
| `docs/*` | Mudancas somente de documentacao. |

## Convencoes de Commit

Conventional Commits sao obrigatorios:

`feat:` `fix:` `docs:` `refactor:` `test:` `chore:` `ci:`

## Fluxo de Pull Request

- Abrir um PR a partir de uma branch seguindo o padrao acima em direcao a `main`.
- Preencher o [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md): descricao, motivacao, impacto, evidencia de teste e checklist.
- O pipeline de CI (`.github/workflows/ci.yml`) deve passar: build, testes unitarios, cobertura do JaCoCo, analise do SonarCloud (quando configurado), e o scan do Trivy.
- O [`.github/CODEOWNERS`](../../.github/CODEOWNERS) determina quem e solicitado para revisao.
- O merge usa o comportamento padrao do GitHub - nenhuma estrategia especifica de merge (squash/merge/rebase) e prescrita aqui.

## Processo de Revisao de Codigo

Toda mudanca deve ser revisada antes do merge - nunca fazer merge sem revisao (`git-rules.md`).

- **Desenvolvimento solo** (fase atual - ver `.github/CODEOWNERS`): o proprio autor realiza a revisao antes do merge, usando a checklist do PR e uma execucao de CI aprovada como base da revisao. Uma revisao assistida por IA (ex.: esta sessao ou `/code-review`) pode servir como uma passagem adicional, nao-humana, mas nao substitui a revisao do proprio autor.
- **Desenvolvimento em equipe** (quando um segundo contribuidor entrar): pelo menos um revisor independente - alguem alem do autor - deve aprovar antes do merge. Atualizar o `.github/CODEOWNERS` de acordo quando isso se aplicar.

## Expectativas de Teste

Conforme `ai_os/.ai/rules/testing-rules.md`:

- Testes unitarios sao sempre obrigatorios; testes de integracao sempre que aplicavel.
- Ferramentas: JUnit 5, Mockito, Testcontainers.
- Priorizar cobertura de casos de uso, logica de dominio, consumers e publishers. Evitar testar getters/setters ou detalhes de implementacao interna - testar comportamento.
- Antes do merge: todos os testes passando, cobertura adequada, nenhum teste fragil/instavel.

## Definicao de Pronto

Ver [`docs/development/definition-of-done.md`](./definition-of-done.md) para a checklist completa por mudanca.
