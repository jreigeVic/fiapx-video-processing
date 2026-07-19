# Demo Frontend

Single static page for demonstrating the platform end to end: login, upload, status tracking, download. Not a build-tooled SPA - no framework, no bundler, no design system. Not subject to architectural review (see roadmap Epic 013); its only purpose is to make the backend demonstrable to the banca.

Adapted from the single-page reference at the FIAP X Go project (`ai_os/.ai/context/source/projeto-fiapx/main.go`'s `getHTMLForm`), with a login step added (the Go reference had none) and calls rewired to this repo's real endpoints (`POST /api/auth/login`, `POST /api/videos`, `GET /api/videos`, `GET /api/videos/{id}/download`).

## Running

Serve it over HTTP - opening `index.html` directly (`file://`) sends `Origin: null`, which CORS will reject:

```bash
cd frontend
python3 -m http.server 5500
# or: npx serve -l 5500
```

Open `http://localhost:5500`, then use the "Configurar endpoints da API" link to point at identity-service and video-service (defaults to `localhost:8081`/`8082` for `docker-compose`; swap in the real LoadBalancer hostnames - `kubectl get svc identity-service video-service -n fiapx` - to demo against the AWS deployment).

## CORS

identity-service and video-service default to no allowed cross-origin browser client (Epic 015, secure-by-default). Whatever origin serves this page must be added to `IDENTITY_CORS_ALLOWED_ORIGINS` / `VIDEO_CORS_ALLOWED_ORIGINS` on the deployed services (e.g. `http://localhost:5500`) before this frontend can call them from a browser.

## Flow

1. Register a user first via the API directly (`POST /api/auth/register` - no register form here, out of scope for the demo) or Swagger UI (`/swagger-ui/index.html` on identity-service).
2. Log in.
3. Upload a video (content-type/size limits per Epic 015 - `video/mp4`, `video/mpeg`, `video/quicktime`, `video/x-msvideo`, `video/x-matroska`; 100MB default max).
4. The list polls every 5s; once a video reaches `PROCESSED`, a Download button appears.
