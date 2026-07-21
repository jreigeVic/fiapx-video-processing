# API Docs

- `openapi.yaml` - versioned HTTP contract for Identity Service and Video Service. Linted in CI (`ci.yml`, `openapi-lint` job).
- `postman_collection.json` - Postman collection generated from `openapi.yaml`. Regenerate after any endpoint change:
  ```bash
  npx --yes openapi-to-postmanv2 -s docs/api/openapi.yaml -o docs/api/postman_collection.json -p
  ```
  CI only verifies the spec still converts successfully (the tool's output includes random UUIDs and randomized examples on every run, so it can never be diffed byte-for-byte against the committed file).

Both Identity Service and Video Service also expose a live, code-generated OpenAPI doc at runtime (Springdoc): `/v3/api-docs` and `/swagger-ui/index.html`, useful for interactive testing against a running instance.

- `authentication.md`, `notification.md`, `processing.md`, `video.md` - narrative documentation per domain area.
