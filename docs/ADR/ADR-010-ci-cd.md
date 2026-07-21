# ADR-010 - CI/CD

## Status

Approved

## Contexto

O HLD define GitHub Actions, Docker, GHCR ou registry equivalente, Kubernetes, Jacoco, SonarQube Cloud e Trivy como base da pipeline.

## Problema

A plataforma precisa de validacao automatizada, build reproduzivel, analise de qualidade, analise de vulnerabilidades e deploy padronizado.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Processo manual | Nao atende entregabilidade e rastreabilidade. |
| CI sem deploy automatizado | Reduz risco, mas nao atende estrategia de CD do HLD. |
| GitHub Actions com pipeline completa | Alinhado ao HLD. |

## Decisao

Utilizar GitHub Actions para CI/CD dos microsservicos, com build, testes, cobertura, analise estatica, scan de vulnerabilidades, build de imagem e deploy em Kubernetes.

## Justificativa

A pipeline automatizada garante qualidade, seguranca, reproducibilidade e entregas consistentes.

## Consequencias

- Cada servico deve ter build e imagem independente.
- Testes unitarios e de integracao devem executar na pipeline.
- Jacoco, SonarQube Cloud e Trivy devem compor as validacoes.
- Deploy deve seguir ambientes progressivos conforme HLD.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Feedback rapido | Configuracao inicial maior. |
| Qualidade automatizada | Pipelines podem ficar mais longas. |
| Rastreabilidade de deploy | Requer disciplina de versionamento. |

## Referencias

- docs/HLD/14-ci-cd.md
- docs/HLD/10-deployment-architecture.md
- .ai/rules/git-rules.md
