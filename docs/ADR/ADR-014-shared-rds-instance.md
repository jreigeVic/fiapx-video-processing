# ADR-014 - Instancia Unica de RDS para os Bancos Logicos

## Status

Approved

## Contexto

ADR-004 define database-per-service. O ambiente de implantacao e o AWS Academy, que impoe restricoes de orcamento e de permissoes (sem criacao de IAM roles, sem OIDC/IRSA).

## Problema

Provisionar uma instancia RDS por microsservico (4 instancias) excede o orcamento e os limites de recursos disponiveis nas contas do AWS Academy.

## Alternativas Avaliadas

| Alternativa | Avaliacao |
|-------------|-----------|
| Uma instancia RDS por servico | Alinhado a ADR-004 na forma mais estrita, mas inviavel no orcamento/limites do AWS Academy. |
| Instancia unica com um banco logico por servico | Mantem o isolamento logico (schema/credenciais/migrations por servico) dentro da restricao de custo. |
| Banco compartilhado entre servicos | Viola database-per-service; acoplaria os servicos pelo schema. Rejeitada. |

## Decisao

Provisionar uma unica instancia `aws_db_instance.postgres` (Terraform, `infrastructure/terraform/database.tf`) hospedando os quatro bancos logicos: `auth_db`, `video_db`, `processing_db`, `notification_db`.

Cada servico continua proprietario exclusivo do seu banco logico: nenhuma credencial ou schema e compartilhado entre servicos, exceto o usuario master usado apenas para a criacao inicial dos bancos.

A criacao dos quatro bancos logicos e feita por um Job de bootstrap do Helm (`infrastructure/helm/cluster-setup`), executado uma vez contra a instancia RDS antes de qualquer release de servico, reaproveitando a mesma logica idempotente de `infrastructure/docker/postgres/init-databases.sh`. As migrations Flyway de cada servico continuam responsaveis apenas pelo schema do seu proprio banco.

## Justificativa

Preserva o isolamento logico exigido por ADR-004 (nenhum servico acessa banco alheio) ao menor custo possivel, dentro das restricoes do AWS Academy (sem IAM role customizada, orcamento limitado).

## Consequencias

- Um unico ponto de falha de infraestrutura (a instancia RDS) para os quatro servicos; aceito como trade-off de ambiente de laboratorio, nao de producao.
- A senha do usuario master (Terraform, `random_password.db`) e usada apenas pelo Job de bootstrap; cada servico usa sua propria `SPRING_DATASOURCE_URL` apontando para o banco logico correspondente.
- Escalar a instancia (classe, IOPS) afeta os quatro servicos simultaneamente.

## Trade-offs

| Beneficio | Trade-off |
|-----------|-----------|
| Custo compativel com AWS Academy | Instancia unica e um ponto de contencao de recursos e disponibilidade |
| Isolamento logico mantido (banco por servico) | Nao ha isolamento fisico entre servicos |

## Referencias

- docs/ADR/ADR-004-database-per-service.md
- infrastructure/terraform/database.tf
- infrastructure/docker/postgres/init-databases.sh
- infrastructure/helm/cluster-setup
