# Definition of Done

A change is done when every applicable item below is satisfied. This checklist follows the six-stage [engineering workflow](./workflow.md#engineering-workflow).

## Design

- [ ] The task is understood; requirements were not invented.
- [ ] Only the files required to complete the task were read.
- [ ] For non-trivial changes, a concise implementation plan was presented before implementation.

## Architecture Review

- [ ] The change respects existing architecture (Clean/Hexagonal Architecture, DDD, Event-Driven Architecture) and the ADRs in `docs/ADR/`.
- [ ] Any architectural change is backed by an approved ADR; consolidated architecture was not altered without one.
- [ ] Approval was obtained before implementing architectural changes.

## Implementation

- [ ] Only what the task requires was implemented — no unrelated scope.
- [ ] Existing code was modified in preference to creating new files, where reasonable.
- [ ] Existing project conventions were followed.

## Validation

- [ ] The project builds successfully.
- [ ] Unit tests pass; integration tests pass where applicable.
- [ ] JaCoCo coverage was generated and reviewed.
- [ ] SonarCloud analysis was run and the quality gate reviewed, where configured.
- [ ] No fragile/flaky tests were introduced.

## Code Review

- [ ] Every change was reviewed before merge (see [Code Review Process](./workflow.md#code-review-process)):
  - Solo development: the author self-reviewed using the PR checklist and a passing CI run.
  - Team development: at least one independent reviewer approved.
- [ ] `.github/PULL_REQUEST_TEMPLATE.md` is filled out (description, motivation, impact, test evidence).

## Commit

- [ ] Commit messages follow Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `ci:`).
- [ ] No secrets or credentials are committed.
- [ ] Documentation was updated where required (HLD/LLD/ADR for architectural changes, per `ai_os/.ai/rules/documentation-rules.md`; `docs/setup/` for platform/infrastructure/CI changes).

---

A new contributor should be able to read this checklist and `docs/development/workflow.md` and understand exactly how to develop inside this project.
