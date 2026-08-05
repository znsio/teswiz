# teswiz Claude Instructions

When working in this repository:

- Keep the repo instructions aligned with the Codex and Antigravity entry points:
  - `.codex/skills/teswiz-project/SKILL.md`
  - `CLAUDE.md`
  - `ANTIGRAVITY.md`
- When you change code or documentation, include a concise suggested commit message in the final response.
- Prefer a short imperative commit message that reflects the main change clearly.
- Keep changes focused and test-first when the task is a refactor or cleanup.
- Prefer small, meaningfully named methods, variables, and classes.
- Keep encapsulation tight; do not widen visibility unless there is a clear framework-facing need.
- For `playwright-ts`, keep test-owned `.ts` screen modules under `src/test/resources/playwright/screens`.
- Prefer internal mobile device-session state under `com.znsio.teswiz.mobile.session` instead of growing `runner`.
- Prefer internal Appium server lifecycle code under `com.znsio.teswiz.mobile.server` instead of growing `runner`.
- For stricter screen-contract audits, use `./gradlew verifyScreenContracts -PincludeMissingScreenTargets=true`.
- Prefer serial focused Gradle verification runs on the same checkout; parallel independent Gradle invocations can produce misleading failures because they share build outputs and intermediates.
