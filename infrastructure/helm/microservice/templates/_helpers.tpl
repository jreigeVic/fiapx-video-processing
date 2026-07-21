{{/*
Release name is always the service name (e.g. "helm install identity-service ./microservice"),
so it doubles as the resource name and the app.kubernetes.io/name label.
*/}}
{{- define "microservice.name" -}}
{{- .Values.nameOverride | default .Release.Name -}}
{{- end -}}

{{- define "microservice.labels" -}}
app.kubernetes.io/name: {{ include "microservice.name" . }}
app.kubernetes.io/part-of: fiapx
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "microservice.selectorLabels" -}}
app.kubernetes.io/name: {{ include "microservice.name" . }}
{{- end -}}
