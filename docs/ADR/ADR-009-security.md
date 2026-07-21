# ADR-009 - Seguranca

## Status

Approved

## Contexto

O HLD define JWT, Spring Security, BCrypt, Kubernetes Secrets, IAM minimo, IRSA, HTTPS e URLs pre-assinadas para S3.

## Problema

A plataforma deve proteger usuarios, credenciais, arquivos de video, resultados processados e recursos cloud.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Autenticacao simples sem tokens | Nao atende seguranca dos recursos protegidos. |
| JWT com Spring Security | Alinhado ao HLD e adequado ao MVP. |
| Servidor de autorizacao dedicado | Nao e a estrategia principal definida para o MVP. |

## Decisao

Utilizar JWT com Spring Security para autenticacao e autorizacao, BCrypt para senhas e segredos gerenciados pelo ambiente.

## Justificativa

A decisao atende aos requisitos de acesso protegido, menor privilegio, protecao de credenciais e acesso controlado aos arquivos em S3.

## Consequencias

- Identity Service autentica usuarios e emite tokens.
- Recursos do Video Service devem exigir token valido.
- Senhas devem ser armazenadas com BCrypt.
- Buckets S3 nao devem ser publicos.
- Downloads devem utilizar URLs pre-assinadas ou fluxo equivalente controlado pelo Video Service.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Modelo simples e adequado ao MVP | Revogacao de tokens exige estrategia futura. |
| Boa integracao com Spring Security | Configuracao deve ser replicada de forma consistente. |
| Protecao de arquivos via URLs temporarias | Exige controle de expiracao e autorizacao. |

## Referencias

- docs/HLD/11-security.md
- .ai/rules/security-rules.md
- .ai/context/stack.md
