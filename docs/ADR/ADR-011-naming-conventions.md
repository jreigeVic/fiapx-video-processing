# ADR-011 — Naming Conventions

Status: Accepted

Date: YYYY-MM-DD

Owner: Software Architect

---

# Context

As the project evolves with multiple microservices, APIs, events, infrastructure resources and documentation, a single naming convention is required to ensure consistency, readability and maintainability.

Without a common convention, documentation and implementation may diverge over time, increasing maintenance cost and reducing traceability.

---

# Decision

The project adopts a single naming strategy for all artifacts.

This ADR defines the official naming conventions for the entire solution.

---

# Java Package Convention

Each microservice shall use its own root package.

Identity Service

com.fiapx.identity

Video Service

com.fiapx.video

Processing Worker

com.fiapx.processing

Notification Service

com.fiapx.notification

No alternative package naming shall be used.

---

# Microservice Naming

Repository folders shall use kebab-case.

Examples

identity-service

video-service

processing-worker

notification-service

---

# Event Naming

Domain events shall use PascalCase.

Examples

VideoUploaded

VideoProcessed

VideoFailed

UserCreated

UserAuthenticated

NotificationSent

Avoid:

video_uploaded

video-uploaded

video.uploaded.v1

Video_Uploaded

Unless explicitly approved by the Software Architect.

---

# API Naming

REST endpoints shall use:

kebab-case

Example

/api/v1/videos

/api/v1/auth

/api/v1/notifications

---

# Database Naming

Logical databases shall use snake_case.

Examples

identity_db

video_db

notification_db

---

# Database Tables

Tables shall use snake_case.

Examples

users

videos

video_processing

notifications

---

# Flyway Naming

Migration files shall follow:

V<version>__<description>.sql

Examples

V001__create_users_table.sql

V002__create_video_table.sql

V003__add_processing_status.sql

---

# Docker Images

Images shall follow:

fiapx/<service-name>

Examples

fiapx/identity-service

fiapx/video-service

fiapx/processing-worker

fiapx/notification-service

---

# Kubernetes

Deployments

identity-service

video-service

processing-worker

notification-service

Services

identity-service

video-service

processing-worker

notification-service

Namespaces

fiapx-dev

fiapx-hml

fiapx-prod

---

# Terraform

Resources shall follow:

<provider>_<resource>_<service>

Example

aws_s3_bucket_video

aws_sqs_processing

aws_sns_video

---

# Git Branches

feature/<name>

bugfix/<name>

hotfix/<name>

release/<version>

---

# Git Tags

v1.0.0

v1.1.0

v2.0.0

Semantic Versioning shall be adopted.

---

# Documentation Naming

Documentation shall use kebab-case.

Examples

architecture-overview.md

video-service.md

processing-worker.md

event-catalog.md

---

# Mermaid

Diagram identifiers shall use PascalCase.

Components shall use official service names.

---

# Benefits

This convention provides:

- consistency;
- traceability;
- readability;
- easier onboarding;
- simpler automation;
- easier AI-assisted generation;
- lower maintenance cost.

---

# Consequences

All future documentation and implementation shall follow this ADR.

Any deviation requires explicit Software Architect approval.

---

# References

High Level Design

Low Level Design

Architecture Rules

Documentation Rules