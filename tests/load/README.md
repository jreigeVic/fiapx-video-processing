# 🧪 Testes de Carga (k6) - Epic 011

Valida o RF-04 e os RNF-01/02/03 com evidência objetiva contra o deploy real (identity-service e video-service, os 2 endpoints públicos de LoadBalancer - [ADR-005](../../docs/ADR/ADR-005-kubernetes.md)/`docs/HLD/10-deployment-architecture.md`, sem Ingress neste ambiente AWS Academy). Conforme decisão aprovada, o `VideoUploadedConsumer` nunca é paralelizado internamente para passar nesses testes - a concorrência vem do SQS competing consumers + múltiplas réplicas de `processing-worker` + HPA, e é exatamente isso que esses cenários demonstram.

## ✅ Pré-requisitos

- [k6](https://k6.io/docs/get-started/installation/) instalado.
- A plataforma implantada (Epic 009/010) com endpoints públicos para identity-service e video-service.
- `kubectl` apontando para o cluster, para o `capture-hpa-evidence.sh`.

## ▶️ Rodando um cenário

```bash
cd tests/load
IDENTITY_BASE_URL="http://<identity-lb-hostname>" \
VIDEO_BASE_URL="http://<video-lb-hostname>" \
k6 run scenario-a-burst.js
```

Repita para `scenario-b-sustained.js` e `scenario-c-spike.js`. Em um segundo terminal, rode `./capture-hpa-evidence.sh` (opcionalmente passando um intervalo em segundos e um nome de arquivo de saída) durante todo o cenário para registrar evidências de HPA/réplicas/pods em paralelo - isso é o que comprova o RNF-02, não só o output do k6.

## 🎬 Fixture

`fixtures/sample.mp4` é um vídeo H.264 real e pequeno (2s, 320x240), gerado com:

```bash
ffmpeg -f lavfi -i "testsrc=duration=2:size=320x240:rate=5" -y fixtures/sample.mp4
```

Real o suficiente para a extração de frames via ffmpeg do `processing-worker` funcionar (diferente de bytes arbitrários, que falham no ffmpeg e terminam em `FAILED`, não `PROCESSED`).

## 📊 Cenários → matriz de evidências RF/RNF

| Cenário | Perfil de carga | Threshold | RF/RNF comprovado |
|---|---|---|---|
| A - Burst | 50 VUs, 1 iteração cada, disparadas juntas | `http_req_failed rate==0`, `checks rate==1` | **RNF-03**: a fila (SNS/SQS) absorve um pico simultâneo de uploads com zero requisições perdidas/falhas |
| B - Sustained | 10 VUs constantes, 5 min, upload em loop | `http_req_failed rate<1%`, `checks rate>99%` | **RF-04/RNF-01**: múltiplos vídeos processados concorrentemente ao longo de uma janela sustentada, via competing consumers - não paralelismo interno do consumer |
| C - Spike | Rampa 5 → 100 → 5 VUs | `http_req_failed rate<5%`, `checks rate>95%` | **RNF-02**: escalonamento horizontal (scale-out e scale-in), evidenciado pelas contagens de réplicas do `capture-hpa-evidence.sh` subindo e depois descendo com o HPA |

RF-01 (upload), RF-06 (status) e RF-03 (download) são exercitados como parte do fluxo de requisições de todo cenário (`lib/video.js`); não são cenários separados porque são o mecanismo, não o objeto, dos testes de carga deste épico.

## 🔍 Interpretando os resultados

- Se um threshold falhar, o resumo impresso pelo k6 informa qual e por quanto - não relativize; ou o ambiente (min/max de réplicas do HPA, capacidade dos nodes) precisa de ajuste, ou - só com evidência, conforme a decisão aprovada - o paralelismo interno do consumer volta a ser discutido como decisão própria.
- O arquivo de log do `capture-hpa-evidence.sh` é o artefato a manter como evidência do RNF-02 (consolidação da Epic 017) - ele mostra a contagem de réplicas ao longo do tempo, não só o estado final.

## 🔎 Documentação relacionada

- [`docs/rf-rnf-traceability.md`](../../docs/rf-rnf-traceability.md) - rastreabilidade completa de RF/RNF × evidências
- [`infrastructure/helm/README.md`](../../infrastructure/helm/README.md) - deploy e HPA dos microsserviços
