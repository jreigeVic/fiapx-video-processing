# Definicao de Pronto (Definition of Done)

Uma mudanca esta pronta quando todos os itens aplicaveis abaixo forem satisfeitos. Esta checklist segue o [fluxo de engenharia](./workflow.md#fluxo-de-engenharia) de seis etapas.

## Design

- [ ] A tarefa foi compreendida; nenhum requisito foi inventado.
- [ ] Apenas os arquivos necessarios para completar a tarefa foram lidos.
- [ ] Para mudancas nao triviais, um plano de implementacao conciso foi apresentado antes da implementacao.

## Revisao de Arquitetura

- [ ] A mudanca respeita a arquitetura existente (Clean/Hexagonal Architecture, DDD, Event-Driven Architecture) e as ADRs em `docs/ADR/`.
- [ ] Qualquer mudanca arquitetural esta amparada por uma ADR aprovada; a arquitetura consolidada nao foi alterada sem uma.
- [ ] A aprovacao foi obtida antes de implementar mudancas arquiteturais.

## Implementacao

- [ ] Apenas o que a tarefa exige foi implementado - sem escopo nao relacionado.
- [ ] Codigo existente foi modificado em vez de criar novos arquivos, quando razoavel.
- [ ] As convencoes existentes do projeto foram seguidas.

## Validacao

- [ ] O projeto compila com sucesso.
- [ ] Testes unitarios passam; testes de integracao passam quando aplicavel.
- [ ] A cobertura do JaCoCo foi gerada e revisada.
- [ ] A analise do SonarCloud foi executada e o quality gate revisado, quando configurado.
- [ ] Nenhum teste fragil/instavel foi introduzido.

## Revisao de Codigo

- [ ] Toda mudanca foi revisada antes do merge (ver [Processo de Revisao de Codigo](./workflow.md#processo-de-revisao-de-codigo)):
  - Desenvolvimento solo: o autor autorrevisou usando a checklist do PR e uma execucao de CI aprovada.
  - Desenvolvimento em equipe: pelo menos um revisor independente aprovou.
- [ ] O `.github/PULL_REQUEST_TEMPLATE.md` foi preenchido (descricao, motivacao, impacto, evidencia de teste).

## Commit

- [ ] As mensagens de commit seguem o Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `ci:`).
- [ ] Nenhum segredo ou credencial foi commitado.
- [ ] A documentacao foi atualizada quando necessario (HLD/LLD/ADR para mudancas arquiteturais, conforme `ai_os/.ai/rules/documentation-rules.md`; `docs/setup/` para mudancas de plataforma/infraestrutura/CI).

---

Um novo contribuidor deve conseguir ler esta checklist e o `docs/development/workflow.md` e entender exatamente como desenvolver dentro deste projeto.
