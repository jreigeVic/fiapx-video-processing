#!/bin/bash
set -e

# Runs alongside any k6 scenario (in a separate terminal) to capture HPA
# and pod resource usage over time - the evidence that RNF-02 (horizontal
# scaling) actually happened, not just that it's configured.
#
# Usage: ./capture-hpa-evidence.sh [interval_seconds] [output_file]

INTERVAL="${1:-10}"
OUTPUT="${2:-hpa-evidence-$(date +%Y%m%d-%H%M%S).log}"

echo "Capturing HPA/pod evidence every ${INTERVAL}s to $OUTPUT (Ctrl+C to stop)"

while true; do
  {
    echo "=== $(date -u +%Y-%m-%dT%H:%M:%SZ) ==="
    kubectl get hpa -n fiapx
    echo
    kubectl get pods -n fiapx -o custom-columns=NAME:.metadata.name,STATUS:.status.phase
    echo
    kubectl top pods -n fiapx 2>/dev/null || echo "(metrics not ready yet)"
    echo
  } | tee -a "$OUTPUT"
  sleep "$INTERVAL"
done
