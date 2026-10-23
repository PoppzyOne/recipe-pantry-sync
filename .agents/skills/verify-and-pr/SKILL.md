---
name: verify-and-pr
description: >-
  Use this skill when verifying code changes, running the full backend and
  frontend test/build pipeline, or preparing and submitting a Pull Request on
  GitHub for recipe-pantry-sync.
---

# Verify & Pull Request Workflow

Run this verification checklist before pushing changes or opening a Pull Request for `PoppzyOne/recipe-pantry-sync`.

## 1. Backend Verification (Quarkus + Maven)
Run the full JUnit / REST-assured integration test suite from `backend/`:
```bash
cd backend && ./mvnw test
```
- Confirm all tests pass (`BUILD SUCCESS`) with 0 failures and 0 errors.

## 2. Frontend Verification (Vue 3 + TypeScript + Vite)
Run type-checking, unit tests, and production bundle verification from `frontend/`:
```bash
cd frontend && npm run type-check && npm run test:unit -- --run && npm run build
```
- Confirm zero TypeScript compiler errors (`vue-tsc`), all Vitest suites pass, and Vite builds cleanly.

## 3. Git & GitHub Pull Request Workflow
1. Ensure work is on a descriptive feature or fix branch (`feature/<name>` or `fix/<name>`), never committed directly to `main`.
2. Stage only intended files (`git status` / `git diff`) and commit with a clear English commit message.
3. Push the branch to `origin`:
   ```bash
   git push -u origin <branch-name>
   ```
4. Use the `github` MCP server (`create_pull_request` on `owner: "PoppzyOne"`, `repo: "recipe-pantry-sync"`) to open a PR against `main`.
5. Check CI status on the PR commit (`get_pull_request_status` or GitHub Actions check runs) before merging.

