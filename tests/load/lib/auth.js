import http from 'k6/http';
import { check } from 'k6';

const IDENTITY_BASE_URL = __ENV.IDENTITY_BASE_URL || 'http://localhost:8081';

// Registers a throwaway user and returns a valid access token. Each VU
// gets its own account (unique email per VU+iteration) so RF-04/RNF-01
// concurrency isn't accidentally serialized by a shared user's rows.
export function registerAndLogin(vuId, iteration) {
  const email = `loadtest-vu${vuId}-${iteration}-${Date.now()}@fiapx.local`;
  const password = 'LoadTest!2026';

  const registerRes = http.post(
    `${IDENTITY_BASE_URL}/api/auth/register`,
    JSON.stringify({ name: `Load Test VU${vuId}`, email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(registerRes, { 'register succeeded': (r) => r.status === 201 });

  const loginRes = http.post(
    `${IDENTITY_BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(loginRes, { 'login succeeded': (r) => r.status === 200 });

  return loginRes.json('accessToken');
}
