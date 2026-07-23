# Dashboard-as-code for Epic 016 (Observability), per ADR-015: the 4 services
# export OTLP traces to New Relic (see each service's OTEL_EXPORTER_OTLP_*
# env vars in infrastructure/helm/microservice/values*.yaml), landing as
# `Span` events in NRQL. This resource turns that raw telemetry into a
# versioned dashboard instead of one hand-built in the UI (harder to
# reproduce, not reviewable in a PR).
resource "newrelic_one_dashboard" "fiapx" {
  name = "FIAP X - Video Processing Platform"

  page {
    name = "Service Overview"

    widget_billboard {
      title  = "Spans received (last 30 min)"
      row    = 1
      column = 1
      width  = 4
      height = 3

      nrql_query {
        account_id = var.new_relic_account_id
        query      = "SELECT count(*) FROM Span FACET service.name SINCE 30 minutes ago"
      }
    }

    widget_line {
      title  = "Request throughput (root spans)"
      row    = 1
      column = 5
      width  = 8
      height = 3

      nrql_query {
        account_id = var.new_relic_account_id
        query      = "SELECT count(*) FROM Span WHERE span.kind IN ('server', 'consumer') FACET service.name TIMESERIES SINCE 30 minutes ago"
      }
    }

    widget_line {
      title  = "p95 latency by service (ms)"
      row    = 4
      column = 1
      width  = 6
      height = 3

      nrql_query {
        account_id = var.new_relic_account_id
        query      = "SELECT percentile(duration.ms, 95) FROM Span WHERE span.kind IN ('server', 'consumer') FACET service.name TIMESERIES SINCE 30 minutes ago"
      }
    }

    widget_line {
      title  = "Error rate by service (%)"
      row    = 4
      column = 7
      width  = 6
      height = 3

      nrql_query {
        account_id = var.new_relic_account_id
        query      = "SELECT percentage(count(*), WHERE otel.status_code = 'ERROR') FROM Span WHERE span.kind IN ('server', 'consumer') FACET service.name TIMESERIES SINCE 30 minutes ago"
      }
    }

    widget_table {
      title  = "Recent distributed traces (upload -> worker -> notification)"
      row    = 7
      column = 1
      width  = 12
      height = 4

      nrql_query {
        account_id = var.new_relic_account_id
        query      = "SELECT trace.id, service.name, name, duration.ms FROM Span WHERE span.kind IN ('server', 'consumer') SINCE 30 minutes ago LIMIT 30"
      }
    }
  }
}
