# 📖 Documentação de API

- `openapi.yaml` - contrato HTTP versionado do Identity Service e do Video Service. Validado no CI (`ci.yml`, job `openapi-lint`).
- `postman_collection.json` - collection do Postman gerada a partir do `openapi.yaml`. Regenere após qualquer alteração de endpoint:
  ```bash
  npx --yes openapi-to-postmanv2 -s docs/api/openapi.yaml -o docs/api/postman_collection.json -p
  ```
  O CI só verifica se o spec ainda converte com sucesso (a saída da ferramenta inclui UUIDs e exemplos aleatórios a cada execução, então nunca pode ser comparada byte a byte com o arquivo commitado).

Tanto o Identity Service quanto o Video Service também expõem documentação OpenAPI viva, gerada a partir do código (Springdoc), em tempo de execução: `/v3/api-docs` e `/swagger-ui/index.html` - útil para testes interativos contra uma instância em execução.

- `authentication.md`, `notification.md`, `processing.md`, `video.md` - documentação narrativa por área de domínio.
