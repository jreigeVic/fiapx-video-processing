# Contributing

Thanks for contributing to the FIAP X Video Processing Platform. This file is a short entry point — the detailed rules live in the linked documents.

## 1. Set up your local environment

- Platform accounts and credentials (placeholders only): [`docs/setup/platform-setup.md`](docs/setup/platform-setup.md)
- Local infrastructure (PostgreSQL, LocalStack via Docker Compose): [`docs/setup/local-development.md`](docs/setup/local-development.md)
- CI pipeline overview: [`docs/setup/ci-cd.md`](docs/setup/ci-cd.md)

## 2. Follow the engineering workflow

Every change goes through: **Design → Architecture Review → Implementation → Validation → Code Review → Commit**.

Full details, branch naming, commit conventions, the Pull Request process, code review policy, testing expectations, and how AI-assisted development fits in: [`docs/development/workflow.md`](docs/development/workflow.md).

## 3. Know when you're done

Before opening a PR, check your change against [`docs/development/definition-of-done.md`](docs/development/definition-of-done.md).

## 4. Working with AI

This repository follows an AI-first engineering workflow. If you're using Claude Code or a similar assistant, it should read `CLAUDE.md` first — it defines the operational contract (task → context → plan → approval → implementation → tests → summary) that AI-assisted changes in this repo follow. `CLAUDE.md` currently lives one directory above this repository, alongside the companion `ai_os` repository, rather than inside `fiapx-video-processing` itself.

## 5. Open a Pull Request

- Use branch names matching `feature/*`, `hotfix/*`, or `docs/*`, off `main`.
- Fill out the PR template ([`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md)) — it's applied automatically.
- Reviewers are determined by [`.github/CODEOWNERS`](.github/CODEOWNERS).
- CI (`.github/workflows/ci.yml`) must pass before merge.

Never commit real credentials — see the Secrets Handling section of `docs/setup/platform-setup.md`.
