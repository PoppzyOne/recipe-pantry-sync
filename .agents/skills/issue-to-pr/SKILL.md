---
name: issue-to-pr
description: >-
  Use this skill to execute the 4-stage multi-agent Issue-to-PR pipeline
  (1. Triage Agent -> 2. Research/Localization Agent -> 3. Coder Agent ->
  4. Review/Test Agent -> Open Pull Request) for a GitHub Issue in
  PoppzyOne/recipe-pantry-sync. Never merges the PR into main.
---

# 4-Stage Issue-to-PR Multi-Agent Pipeline

Run this orchestrated 4-agent workflow when implementing a GitHub Issue from start to Pull Request.

```text
[GitHub Issue]
      │
      ▼
1. Triage Agent        (.agents/agents/triage-agent.md)
      │
      ▼
2. Research Agent      (.agents/agents/research-agent.md)
      │
      ▼
3. Coder Agent         (.agents/agents/coder-agent.md)
      │
      ▼
4. Review & Test Agent (.agents/agents/review-test-agent.md)
      │
      ▼
[Open Pull Request linked to Issue — NEVER auto-merge]
```

## Stage 1: Triage Agent (`triage-agent`)
1. Fetch the target issue and existing open issues via the `github` MCP server (`get_issue`, `list_issues` on `owner: "PoppzyOne"`, `repo: "recipe-pantry-sync"`).
2. Delegate to a subagent (or follow `.agents/agents/triage-agent.md`) to:
   - Classify the issue (`bug` vs `feature` vs `refactor`).
   - Check for duplicate issues or missing information.
   - Write `.agent-workspace/triage.json`.
3. If `actionable` is `false` (duplicate or missing critical info), post a clarifying comment on the GitHub issue (`add_issue_comment`) and **stop the pipeline**.

## Stage 2: Research & Localization Agent (`research-agent`)
1. Invoke a research subagent with `.agents/agents/research-agent.md` and `.agent-workspace/triage.json`.
2. Search `backend/` and `frontend/` (and use `exa` / `context7` / `github` MCP tools if needed) to identify every affected file, DTO, entity, store, and test.
3. Save the implementation specification to `.agent-workspace/research-plan.md`.

## Stage 3: Coder Agent (`coder-agent`)
1. Ensure the working tree is on a fresh branch created from `main`:
   `<branchPrefix>/issue-<issueNumber>-<slug>` (e.g. `feat/issue-12-meal-export` or `fix/issue-15-unit-conversion`).
2. Invoke a coder subagent with `.agents/agents/coder-agent.md` and `.agent-workspace/research-plan.md`.
3. Implement all backend and frontend changes and write corresponding JUnit/REST-assured and Vitest tests.

## Stage 4: Review & Test Agent (`review-test-agent`)
1. Invoke a review/test subagent with `.agents/agents/review-test-agent.md`.
2. Run the full test and build verification suite:
   - `cd backend && ./mvnw test --batch-mode`
   - `cd frontend && npm run type-check && npx vitest run && npm run build`
3. Audit `git diff` against `AGENTS.md` (Java `record` DTOs, no exposed JPA entities, 1:1 TypeScript parity, offline-first Pinia persistence) and write `.agent-workspace/pr-body.md`.

## Final Step: Create Pull Request (Human Approval Required)
1. Clean up `.agent-workspace/` (or keep it untracked via `.gitignore`), stage only the intended source/test files, commit, and push the branch to `origin`.
2. Call `create_pull_request` via the `github` MCP server with base `main` and head `<branchPrefix>/issue-<issueNumber>-<slug>`, including `Closes #<issueNumber>` in the body.
3. Comment on the issue with a link to the opened Pull Request.
4. **STRICT GUARDRAIL:** Never merge the Pull Request into `main`. Only `PoppzyOne` is permitted to approve and merge PRs.

