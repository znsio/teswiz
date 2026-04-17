---
name: teswiz-project
description: Use when working in the znsio/teswiz repository to modify framework code, Cucumber/TestNG hooks, Applitools visual testing flows, configs/caps, or related docs/tests. Covers repo-specific file layout, verification commands, and current conventions such as Figma-driven Applitools naming instead of @eyes tags.
---

# Teswiz Project

Use this skill for changes inside the `znsio/teswiz` repo.

## Repo map

- Core framework code: `src/main/java/com/znsio/teswiz`
- Step definitions used by sample tests: `src/main/java/com/znsio/teswiz/steps`
- Unit tests: `src/test/java/com/znsio/teswiz`
- Feature files: `src/test/resources/com/znsio/teswiz/features`
- Execution configs: `configs/<app>/...`
- Capability files: `caps/<app>/...`
- Visual-testing docs: `docs/RunningVisualTests-README.md`

## Working conventions

- Prefer `rg` for code and file discovery.
- Use `apply_patch` for manual source edits.
- Do not revert unrelated worktree changes.
- Favor focused Gradle verification over broad test runs when touching a narrow area.

## Visual testing rules

- Applitools config keys live in:
  `src/main/java/com/znsio/teswiz/entities/APPLITOOLS.java`
- Test execution context keys live in:
  `src/main/java/com/znsio/teswiz/entities/TEST_CONTEXT.java`
- Visual setup and Eyes creation live in:
  `src/main/java/com/znsio/teswiz/runner/Visual.java`

### Current Applitools naming convention

- Do not use scenario tags like `@eyes-...` for baseline naming.
- The supported flow is the explicit Figma step in:
  `src/main/java/com/znsio/teswiz/steps/FigmaSteps.java`
- That step stores:
  - `APPLITOOLS_FIGMA_APP_NAME`
  - `APPLITOOLS_FIGMA_TEST_NAME`
  - `APPLITOOLS_FIGMA_BASELINE_ENV_NAME`
- `Visual` must treat those three values as all-or-nothing:
  - if all are non-blank, use them before `eyes.open(...)`
  - if some are present and some are missing/blank, fail with `VisualTestSetupException`
  - if none are present, use the default teswiz naming flow

### NML and UFG

- UFG test-specific setup is currently added from:
  `src/test/java/com/znsio/teswiz/steps/RunTestCukes.java`
- Native Mobile Layout uses `useNML` and `APPLITOOLS.NML_CONFIG`
- When adjusting NML device handling, keep support for one or many device targets.

## Testing guidance

- For compile-only validation:
  `./gradlew -q compileTestJava`
- For focused unit tests, prefer:
  `./gradlew -q test --tests <fully.qualified.TestClass>`
- Useful recent targets:
  - `com.znsio.teswiz.runner.VisualTest`
  - `com.znsio.teswiz.steps.FigmaStepsTest`

## Documentation guidance

- If behavior changes for visual testing, update both:
  - `README.md`
  - `docs/RunningVisualTests-README.md`
- Keep docs aligned with the current supported flow; remove stale references rather than documenting both old and new patterns.

## Maintenance

- Update this skill whenever repo conventions change for:
  - Applitools naming
  - step-definition ownership
  - preferred verification commands
  - config/caps layout
