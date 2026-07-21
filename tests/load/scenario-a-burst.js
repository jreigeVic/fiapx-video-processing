// Cenario A - Burst (RNF-03): 50 uploads simultaneos. Proves the queue
// (SNS/SQS) absorbs a traffic spike without losing any request - every
// upload call must be accepted (202), none dropped/errored.
import { registerAndLogin } from './lib/auth.js';
import { uploadVideo } from './lib/video.js';

export const options = {
  scenarios: {
    burst: {
      executor: 'per-vu-iterations',
      vus: 50,
      iterations: 1,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate==0'],
    checks: ['rate==1'],
    http_req_duration: ['p(95)<5000'],
  },
};

export default function () {
  const token = registerAndLogin(__VU, __ITER);
  if (!token) {
    return;
  }
  uploadVideo(token);
}
