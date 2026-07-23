# Convencoes de Eventos

Eventos devem seguir nomenclatura PascalCase (ex.: VideoUploaded, VideoProcessed, VideoFailed).

Todo evento deve incluir um envelope com eventId, eventType, occurredAt, correlationId, producer, payload.

Consultar ADR-011 e as secoes de eventos das LLDs para detalhes.
