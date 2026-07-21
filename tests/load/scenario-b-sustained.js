// Cenario B - Sustained Load (RF-04, RNF-01): 10 usuarios simultaneos por
// 5 minutos, cada um enviando videos em loop. Prova concorrencia real:
// com 10 VUs enviando continuamente, multiplos videos ficam PROCESSING ao
// mesmo tempo, absorvidos pelos multiplos replicas do processing-worker
// (competing consumers + HPA) sem paralelismo interno no consumer.
import { sleep } from 'k6';
import { registerAndLogin } from './lib/auth.js';
import { uploadVideo } from './lib/video.js';

export const options = {
  scenarios: {
    sustained: {
      executor: 'constant-vus',
      vus: 10,
      duration: '5m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
    http_req_duration: ['p(95)<5000'],
  },
};

// Module-level (per-VU) cache: k6 runs the script in its own JS context
// per VU, so this persists across a VU's iterations but resets per VU.
let token;

export default function () {
  if (!token) {
    token = registerAndLogin(__VU, __ITER);
  }
  if (token) {
    uploadVideo(token);
  }
  sleep(2);
}
