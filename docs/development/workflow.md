# Development Workflow

This document standardizes how development is performed in this repository, so any contributor — human or AI-assisted — can work consistently. It does not cover application code, infrastructure, Docker, Kubernetes, AWS, or CI/CD implementation; see [`docs/setup/`](../setup/) for those.

---

## Engineering Workflow

Every change — whether written by a human, by AI, or by both — follows the same six stages:

```
Design
  ↓
Architecture Review
  ↓
Implementation
  ↓
Validation
  ↓
Code Review
  ↓
Commit
```

1. **Design** — Understand the task, read the relevant context (`CLAUDE.md`, the task description, existing ADRs/HLD/LLD, and only the source files required), and present a concise implementation plan before writing code.
2. **Architecture Review** — Confirm the plan respects existing architecture (Clean/Hexagonal Architecture, DDD, Event-Driven Architecture, and the ADRs in `docs/ADR/`). Any architectural change requires an approved ADR — never change consolidated architecture silently. Wait for explicit approval before proceeding when the change touches architecture.
3. **Implementation** — Implement only what the task requires. Prefer modifying existing code over creating new files. Follow existing project conventions.
4. **Validation** — Build, run unit tests (and integration tests where applicable), generate JaCoCo coverage, and fix failures. See [Testing Expectations](#testing-expectations) below.
5. **Code Review** — Every change is reviewed before merge. See [Code Review Process](#code-review-process) below.
6. **Commit** — Commit with a Conventional Commit message (see below). No secrets committed. Documentation updated where required (see [Definition of Done](./definition-of-done.md)).

### AI-assisted development

This repository follows an AI-first engineering workflow: architectural decisions are made by engineers, and repetitive implementation is accelerated through AI. When AI assists with any of the six stages above, it follows:

- **`CLAUDE.md`** (lives one directory above this repository, alongside the companion `ai_os` repository) — the operational contract for AI-assisted work in this repository: read the task, read only the files required, analyze, present a plan, wait for approval before architectural changes, implement, run tests and fix failures, summarize. This maps directly onto the Design → Architecture Review → Implementation → Validation stages above.
- **`ai_os/.ai/rules/ai-workflow-rules.md`** (companion AI OS repository) — the broader AI workflow policy: consult `.ai/context` and `.ai/rules` before generating code, never assume requirements that don't exist, ask when ambiguous, never alter consolidated architecture without an ADR, and keep documentation synchronized.

AI-assisted changes are held to the same Code Review and Definition of Done as any other change — AI involvement does not skip review.

---

## Branch Strategy

Per `ai_os/.ai/rules/git-rules.md`:

| Branch pattern | Purpose |
|---|---|
| `main` | Always stable. |
| `feature/*` | New functionality. |
| `hotfix/*` | Critical fixes. |
| `docs/*` | Documentation-only changes. |

## Commit Conventions

Conventional Commits are required:

`feat:` `fix:` `docs:` `refactor:` `test:` `chore:` `ci:`

## Pull Request Workflow

- Open a PR from a branch following the pattern above into `main`.
- Fill out [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md): description, motivation, impact, test evidence, and checklist.
- The CI pipeline (`.github/workflows/ci.yml`) must pass: build, unit tests, JaCoCo coverage, SonarCloud analysis (when configured), and the Trivy scan.
- [`.github/CODEOWNERS`](../../.github/CODEOWNERS) determines who is requested for review.
- Merging uses the repository's default GitHub merge behavior — no specific merge strategy (squash/merge/rebase) is prescribed here.

## Code Review Process

Every change must be reviewed before merge — never merge without review (`git-rules.md`).

- **Solo development** (current phase — see `.github/CODEOWNERS`): the author performs the review themselves before merging, using the PR checklist and a passing CI run as the review basis. AI-assisted review (e.g. this session or `/code-review`) may serve as an additional, non-human pass, but does not replace the author's own review.
- **Team development** (once a second contributor joins): at least one independent reviewer — someone other than the author — must approve before merge. Update `.github/CODEOWNERS` accordingly when this applies.

## Testing Expectations

Per `ai_os/.ai/rules/testing-rules.md`:

- Unit tests are always required; integration tests wherever applicable.
- Tools: JUnit 5, Mockito, Testcontainers.
- Prioritize coverage of use cases, domain logic, consumers, and publishers. Avoid testing getters/setters or internal implementation details — test behavior.
- Before merge: all tests passing, adequate coverage, no fragile/flaky tests.

## Definition of Done

See [`docs/development/definition-of-done.md`](./definition-of-done.md) for the full per-change checklist.
