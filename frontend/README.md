# 🖥️ Frontend de Demonstração

Página estática única para demonstrar a plataforma de ponta a ponta: login, upload, acompanhamento de status e download. Não é uma SPA com ferramentas de build - sem framework, sem bundler, sem design system. Não está sujeita a revisão arquitetural (ver Epic 013 do roadmap); seu único propósito é tornar o backend demonstrável para a banca.

Adaptada da página de referência do projeto FIAP X em Go (`ai_os/.ai/context/source/projeto-fiapx/main.go`, função `getHTMLForm`), com uma etapa de login adicionada (a referência em Go não tinha) e as chamadas redirecionadas para os endpoints reais deste repositório (`POST /api/auth/login`, `POST /api/videos`, `GET /api/videos`, `GET /api/videos/{id}/download`).

## ▶️ Como rodar

Sirva por HTTP - abrir `index.html` direto (`file://`) envia `Origin: null`, que o CORS rejeita:

```bash
cd frontend
python3 -m http.server 5500
# ou: npx serve -l 5500
```

Abra `http://localhost:5500` e use o link "Configurar endpoints da API" para apontar para o identity-service e o video-service (padrão `localhost:8081`/`8082` para `docker-compose`; troque pelos hostnames reais do LoadBalancer - `kubectl get svc identity-service video-service -n fiapx` - para demonstrar contra o deploy na AWS).

## 🔐 CORS

Por padrão, identity-service e video-service não liberam nenhuma origem cross-origin para navegador (Epic 015, seguro por padrão). A origem que serve esta página precisa ser adicionada em `IDENTITY_CORS_ALLOWED_ORIGINS` / `VIDEO_CORS_ALLOWED_ORIGINS` nos serviços implantados (ex.: `http://localhost:5500`) antes que este frontend consiga chamá-los a partir do navegador.

## 🔄 Fluxo de uso

1. Registre um usuário primeiro direto pela API (`POST /api/auth/register` - não há formulário de registro aqui, fora do escopo da demo) ou pelo Swagger UI (`/swagger-ui/index.html` no identity-service).
2. Faça login.
3. Envie um vídeo (limites de tipo/tamanho conforme Epic 015 - `video/mp4`, `video/mpeg`, `video/quicktime`, `video/x-msvideo`, `video/x-matroska`; máximo padrão de 100MB).
4. A lista atualiza a cada 5s; quando um vídeo chega em `PROCESSED`, o botão de Download aparece.

## 🔎 Documentação relacionada

- [`docs/api/authentication.md`](../docs/api/authentication.md) - contrato de login/registro
- [`docs/api/video.md`](../docs/api/video.md) - contrato de upload/status/download
