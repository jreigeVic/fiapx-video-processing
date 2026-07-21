// Cenario C - Spike (RNF-02): 5 -> 100 -> 5 usuarios. Objetivo: forcar o
// HPA a escalar o processing-worker (e os demais servicos) para cima
// durante o pico e depois para baixo - ver
// tests/load/capture-hpa-evidence.sh, que deve rodar em paralelo a este
// cenario para registrar as replicas ao longo do tempo.
import { sleep } from 'k6';
import { registerAndLogin } from './lib/auth.js';
import { uploadVideo } from './lib/video.js';

export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 5,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '30s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.95'],
  },
};

let token;

export default function () {
  if (!token) {
    token = registerAndLogin(__VU, __ITER);
  }
  if (token) {
    uploadVideo(token);
  }
  sleep(1);
}
