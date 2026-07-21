import http from 'k6/http';
import { check } from 'k6';

const VIDEO_BASE_URL = __ENV.VIDEO_BASE_URL || 'http://localhost:8082';
const FIXTURE = open('../fixtures/sample.mp4', 'b');

export function authHeaders(token) {
  return { Authorization: `Bearer ${token}` };
}

// RF-01: upload. Returns the created video's id, or null if the upload
// itself failed (thresholds on http_req_failed/checks catch that).
export function uploadVideo(token) {
  const res = http.post(
    `${VIDEO_BASE_URL}/api/videos`,
    { file: http.file(FIXTURE, 'sample.mp4', 'video/mp4') },
    { headers: authHeaders(token) },
  );
  const ok = check(res, { 'upload accepted (202)': (r) => r.status === 202 });
  return ok ? res.json('id') : null;
}

// RF-06: status listing/lookup by id.
export function getVideoStatus(token, videoId) {
  const res = http.get(`${VIDEO_BASE_URL}/api/videos/${videoId}`, {
    headers: authHeaders(token),
  });
  check(res, { 'get video status succeeded': (r) => r.status === 200 });
  return res.json('status');
}

export function listVideos(token) {
  const res = http.get(`${VIDEO_BASE_URL}/api/videos`, { headers: authHeaders(token) });
  check(res, { 'list videos succeeded': (r) => r.status === 200 });
  return res.json();
}

// RF-03: download URL.
export function getDownloadUrl(token, videoId) {
  const res = http.get(`${VIDEO_BASE_URL}/api/videos/${videoId}/download`, {
    headers: authHeaders(token),
  });
  check(res, { 'download url succeeded': (r) => r.status === 200 });
  return res;
}
