# Load Testing (k6) - Epic 011

Validates RF-04 and RNF-01/02/03 with objective evidence against the real
deployment (identity-service and video-service, the 2 public LoadBalancer
endpoints - ADR-005/HLD-10, no Ingress in this AWS Academy environment).
Per the approved decision, `VideoUploadedConsumer` is never parallelized
internally to pass these tests - concurrency comes from SQS competing
consumers + multiple `processing-worker` replicas + HPA, and that is
exactly what these scenarios are meant to demonstrate.

## Prerequisites

- [k6](https://k6.io/docs/get-started/installation/) installed.
- The platform deployed (Epic 009/010) with public endpoints for
  identity-service and video-service.
- `kubectl` pointed at the cluster, for `capture-hpa-evidence.sh`.

## Running a scenario

```bash
cd tests/load
IDENTITY_BASE_URL="http://<identity-lb-hostname>" \
VIDEO_BASE_URL="http://<video-lb-hostname>" \
k6 run scenario-a-burst.js
```

Repeat for `scenario-b-sustained.js` and `scenario-c-spike.js`. In a
second terminal, run `./capture-hpa-evidence.sh` (optionally passing an
interval in seconds and an output file name) for the duration of each
scenario to record HPA/replica/pod evidence in parallel - this is what
proves RNF-02, not just the k6 output.

## Fixture

`fixtures/sample.mp4` is a tiny (2s, 320x240) real H.264 video generated
with:

```bash
ffmpeg -f lavfi -i "testsrc=duration=2:size=320x240:rate=5" -y fixtures/sample.mp4
```

Real enough for `processing-worker`'s ffmpeg frame extraction to succeed
(unlike arbitrary bytes, which fail ffmpeg and end in `FAILED`, not
`PROCESSED`).

## Scenarios -> RF/RNF evidence matrix

| Scenario | Load profile | Threshold | RF/RNF proven |
|---|---|---|---|
| A - Burst | 50 VUs, 1 iteration each, fired together | `http_req_failed rate==0`, `checks rate==1` | **RNF-03**: the queue (SNS/SQS) absorbs a simultaneous upload spike with zero lost/failed requests |
| B - Sustained | 10 constant VUs, 5 min, uploading in a loop | `http_req_failed rate<1%`, `checks rate>99%` | **RF-04/RNF-01**: multiple videos processed concurrently over a sustained window, via competing consumers - not internal consumer parallelism |
| C - Spike | Ramping 5 -> 100 -> 5 VUs | `http_req_failed rate<5%`, `checks rate>95%` | **RNF-02**: horizontal scale-out and scale-in, evidenced by `capture-hpa-evidence.sh`'s replica counts rising then falling with the HPA |

RF-01 (upload), RF-06 (status) and RF-03 (download) are exercised as
part of every scenario's request flow (`lib/video.js`); they aren't
separate scenarios since they're the mechanism, not the object, of this
epic's load tests.

## Interpreting results

- If a threshold fails, the summary printed by k6 states which one and
  by how much - do not average it away; either the environment (HPA
  min/max replicas, node capacity) needs adjustment, or - only with
  evidence, per the approved decision - internal consumer parallelism
  gets reopened as its own decision.
- `capture-hpa-evidence.sh`'s log file is the artifact to keep as
  evidence for RNF-02 (Epic 017 consolidation) - it shows replica counts
  over time, not just the final state.
