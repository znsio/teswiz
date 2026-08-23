# TestNG-only Execution Mode — Implementation Plan & Checklist

**Branch:** `direct-testng`
**Status:** Phase 0, Phase 1 (walking skeleton), and Phase 2 (dynamic test-class discovery) complete and verified end-to-end
**Last updated:** 2026-08-23

This is a living document. As each checklist item is completed, tick it and add a one-line note (commit reference once committed). Do not delete completed items — this is the running record of what's done and what's left.

## Context

teswiz today only supports Cucumber-JVM (`.feature` files → step defs → business/screen layers), executed via `AbstractTestNGCucumberTests`. Some consumers don't need Gherkin's collaborative/BDD layer — they want to write plain TestNG tests that call the same business/screen layers directly, skipping the step-def translation layer.

This adds a **second, TestNG-only execution mode**, selected per-consumer via the (currently dead) `FRAMEWORK` config property. No JUnit. No coexistence within a single run — a consumer's config picks exactly one mode; both modes' code may exist in the framework simultaneously. Existing Cucumber consumers are unaffected and are **not** being migrated as part of this work.

Two phases:
- **Phase 0**: prerequisite cleanup — align the two diverged `AspectLogging.java` copies (kept as two classes by design, not merged — see decisions below).
- **Phase 1**: walking skeleton proving the new mode's core mechanics (config-driven mode fork, hooks, driver/session reuse, tags→groups, parallel execution, data-driven tests) using plain log4j only. ReportPortal step-level logging and a tag-coverage HTML report are explicitly out of scope — later phases.

TDD throughout: a failing test before the implementation that makes it pass. Small, meaningfully-named methods/classes; no speculative abstractions. Global project convention: never auto-commit — a suggested commit message is provided after each logical step, left staged/unstaged for manual review and commit.

---

## Decisions locked in

| Question | Decision |
|---|---|
| Runner: TestNG, JUnit, or both? | **TestNG only.** |
| Mode selection mechanism | **Wire up the existing dead `FRAMEWORK` config property** (`cucumber` \| `testng`), read via the same `getOverriddenStringValue(...)` pattern already used for other flags (e.g. `SET_HARD_GATE`). `FRAMEWORK` already exists in nearly every `configs/**/*.properties` file (value `cucumber` almost everywhere, already `testng` in `configs/notepad/notepad_windows_config.properties`) but nothing reads it today. Default/fallback stays `cucumber`. |
| Phase 1 scope | **Walking skeleton only** — plain log4j, no RP step-level logging, no tag-coverage HTML report (later phases). |
| AspectLogging | **Two classes stay separate by design**, not merged — but they must have **distinct class names** (see below). `src/main`'s `AspectLogging` (framework-internal packages: `entities`, `listener`, `runner`, `tools`) stays at **DEBUG**. A renamed `src/test` class, `ConsumerLayerAspectLogging` (consumer-authored packages: `steps`, `businessLayer`, `screen`), is **INFO** — a teswiz user should see detail about *their own* implementation by default. Pointcuts aligned so `steps` scope moves fully to the `ConsumerLayerAspectLogging` (INFO) aspect, broadened from `steps.*Steps.*` to `steps.*.*`. Unused injected `TestExecutionContext`/`SessionContext` in the `src/test` copy is dropped (dead state). |
| **Discovery**: same-name shadowing bug | The two classes originally shared the identical FQN `com.znsio.teswiz.aspect.AspectLogging`. Verified empirically that `./gradlew test` loads only the `src/test` copy (it shadows `src/main`'s class on the classpath) — meaning **every** woven call site in a test run, including ones woven under `src/main`'s pointcut, invoked the `src/test` copy's advice body. The two were never simultaneously active; it was a fragile, untested, implicit override, not a working two-tier design. **Fix: rename the `src/test` class to `ConsumerLayerAspectLogging`** so both aspects are genuinely distinct, independently woven, and simultaneously verifiable in one JVM. |
| Parallel execution / `testng.xml` | Hard requirement, **configured entirely programmatically** via `org.testng.TestNG`'s API. **No `testng.xml` file is ever created or checked in** for a consumer to maintain — any `XmlSuite`/`XmlTest` object the API needs internally is built and used in memory only. |
| Data-driven testing | Hard requirement, via TestNG `@DataProvider`, piloted against `cyptoAPI.feature`'s Scenario Outline (`cli.feature`/`InteractiveCalculatorCLIBL` is unsuitable — its assertions are hardcoded to one fixed scripted transcript, not genuinely parameterizable). |
| Retry-on-failure | Not needed — no work planned. |
| Tag → TestNG group mapping | **Simple inclusion only in Phase 1.** Cucumber's `and`/`not` boolean tag-expression support is explicitly deferred (see Backlog). |
| Existing Cucumber consumers | **Not migrated** as part of this work. A dedicated migration skill/agent for one-time conversions is a separate, future, lower-priority effort. |
| New TestNG-mode package | `com.znsio.teswiz.testng` (mirrors this repo's convention of dedicated packages per concern, e.g. `com.znsio.teswiz.mobile.session`). |
| New Gradle task? | **No new task.** The existing `run` JavaExec task stays the single entry point for both modes; `FRAMEWORK` in the config/env controls which mode runs. |

---

## Phase 0 — Align `AspectLogging` scope

- [x] **0.0** Discovered and confirmed (empirically, via a throwaway probe test) that the two classes shared an identical FQN and only one was ever loaded per JVM — `src/test`'s copy silently shadowed `src/main`'s during `./gradlew test`. Decision taken: rename the `src/test` copy to `ConsumerLayerAspectLogging` so both aspects are genuinely distinct and simultaneously active/testable.
- [x] **0.1** Write failing weaving-verification test: `src/test/java/com/znsio/teswiz/aspect/AspectLoggingWeavingTest.java`
  - Log4j2 programmatic test appender (`AbstractAppender`) attached to the shared `AspectJMethodLoggers` logger (both aspects delegate their actual logging calls to this one helper's logger, so a single appender observes both).
  - A `runner`-package call → captured at `DEBUG` via `AspectLogging`. Required a new minimal fixture `src/main/java/com/znsio/teswiz/runner/AspectLoggingProbe.java` — real `runner`-package methods (e.g. `Runner.isRunningInCI()`) all depend on `Setup` being fully loaded first and NPE otherwise, so a dedicated no-dependency fixture was needed. It must live in `src/main` (self-woven at `compileJava` time) — a `src/test`-side fixture in the `runner` package would need extra `testAspect`/aspectpath Gradle wiring to be woven by a main-sourceSet aspect, confirmed by hitting `[error] aspect '...AspectLogging' woven into '...' must be defined to the weaver` when first tried.
  - A `businessLayer` call → captured at `INFO` via `ConsumerLayerAspectLogging`, using a new minimal fixture `src/test/java/com/znsio/teswiz/businessLayer/aspectfixture/AspectFixtureBL.java` (real business-layer classes like `CryptoAPIBL`/`InteractiveCalculatorCLIBL` need full `Setup`/test-data context, too heavy for a fast unit test).
  - A call outside both scopes (`AspectJMethodLoggers.generateAfterMethodAspectJLogger(...)` itself, in the `aspect` package) → nothing captured.
  - Confirmed red against the pre-fix state — and for the *correct* reason: the runner-package call produced no `DEBUG` message at all, because at the time everything was still logging at `INFO` (the shadowing bug in action), not because of some unrelated setup problem.
- [x] **0.2** Narrowed `src/main/java/com/znsio/teswiz/aspect/AspectLogging.java` pointcut to `entities`, `listener`, `runner`, `tools`; kept `DEBUG`.
- [x] **0.3** Renamed `src/test/java/com/znsio/teswiz/aspect/AspectLogging.java` → `ConsumerLayerAspectLogging.java` (via `git mv`, class + file). Pointcut is now `steps.*.*` (broadened from `steps.*Steps.*`), `businessLayer.*.*.*`, `screen.*.*.*.*`; kept `INFO`; removed the unused constructor and its injected `TestExecutionContext`/`SessionContext` fields — the class is now stateless.
- [x] **0.4** Confirmed green: `AspectLoggingWeavingTest` passes (3/3), and the full `./gradlew test` suite shows 37 pre-existing, unrelated failures (Playwright browser binaries missing locally, GitHub sample-APK download timing out — no network/sandbox access for those in this environment) and 0 failures in the `aspect` package or anywhere touched by this change.
- [x] **0.5** Updated `docs/features/AspectJLogging-README.md` — added a table describing the two aspects (source set, packages, level), and documented the same-FQN shadowing bug and its fix for anyone reading history.
- [x] **0.6** Suggested commits ready for manual commit (below) — Phase 0 complete, nothing committed automatically per this repo's standing convention (changes left staged/unstaged for review).

**Suggested commits (Phase 0):**
```
test(aspect): add weaving-verification test covering both AspectLogging aspects

Add AspectLoggingWeavingTest plus two minimal fixtures
(AspectLoggingProbe in src/main, AspectFixtureBL in src/test) needed
because real runner/business-layer classes require full Setup/test-data
context that would make this an unnecessarily slow, brittle unit test.
Confirms red: today every woven call logs at INFO regardless of
package, due to a same-FQN shadowing bug between the two AspectLogging
copies (see next commit).
```
```
refactor(aspect): rename src/test AspectLogging to ConsumerLayerAspectLogging

Fixes a same-FQN shadowing bug where the src/test copy silently
overrode the src/main copy during test runs, making only one aspect
ever active despite two being defined. Renaming makes both aspects
genuinely distinct and simultaneously verifiable, while realigning
pointcut scope: AspectLogging (DEBUG) keeps entities/listener/runner/
tools; ConsumerLayerAspectLogging (INFO) covers steps/businessLayer/
screen and drops its unused injected TestExecutionContext/
SessionContext fields.
```
```
docs(aspect): document the two AspectLogging aspects and the shadowing fix
```

---

## Phase 1 — TestNG-only mode walking skeleton

- [x] **1.1** `Setup.java`: added `FRAMEWORK`/`FRAMEWORK_CUCUMBER`/`FRAMEWORK_TESTNG` constants, read via existing `getOverriddenStringValue` pattern (defaulting to `cucumber`), added `isTestNgExecutionMode()`. Test-first: `SetupFrameworkModeTest` — no key from an existing `cucumber`-configured file → false; `notepad_windows_config.properties` (already has `FRAMEWORK=testng` sitting unused) → true; case-insensitive override via `-D`; unrecognized value falls back to Cucumber. All green.
- [x] **1.2** `build.gradle`: added a direct `org.testng:testng:7.12.0` dependency (pinned to the version already resolved transitively via `cucumber-testng`).
- [x] **1.3** New `src/main/java/com/znsio/teswiz/testng/TestNgRunner.java` — programmatic `org.testng.TestNG` bootstrap (test classes, groups, parallel mode, thread count, registers `TeswizTestNgListener`, default listeners disabled). Test-first: `TestNgRunnerTest` + fixtures `AlwaysPassingTestNgTest`, `AlwaysFailingTestNgTest`, `ThreadRecordingTestNgTest` (proves >1 thread ID observed at thread count 2). All green.
- [x] **1.4a — discovered mid-implementation, not in original plan**: `Hooks.beforeScenario`/`ScreenShotManager` depend on per-test directory state (`SCREENSHOT_DIRECTORY`, `SCENARIO_LOG_DIRECTORY`, `DEVICE_LOGS_DIRECTORY`, `NORMALISED_SCENARIO_NAME`, `SCENARIO_RUN_COUNT`) that Cucumber mode populates via `CucumberScenarioListener.scenarioStartedHandler` — nothing did this for TestNG mode, so `new Hooks().beforeScenario(...)` crashed with `InvalidTestDataException: Directory is null or empty` the moment a real `ScreenShotManager` was constructed. Added `src/main/java/com/znsio/teswiz/testng/TestNgTestExecutionContextFactory.java`, mirroring `CucumberScenarioListener`'s directory-setup logic (reusing the same `FileUtils`/`OsUtils`/`StringUtils`/`FileLocations` utilities). Test-first: `TestNgTestExecutionContextFactoryTest`. Green.
- [x] **1.4b** `Runner.java` fork — pending (next).
- [x] **1.5** `Drivers.java`: added `attachLogsAndCloseAllDrivers(boolean failed)` overload (existing `Scenario`-based overload now delegates to it), decoupling `updateTestStatusInCloud` from Cucumber's `Status` enum. `Hooks.java`: added `beforeScenario(String)` / `afterScenario(String, boolean)` overloads (existing `Scenario`-based overloads delegate to these, behavior unchanged). New `src/main/java/com/znsio/teswiz/testng/TeswizTestNgListener.java` (`ITestListener`) wiring TestNG lifecycle callbacks to `TestNgTestExecutionContextFactory` + the new `Hooks` overloads, with a running-test counter. Test-first: `HooksTestNgOverloadTest` proving state parity with the `Scenario`-based path (both use the same context factory now). All green.
- [x] **1.6** `Setup.java`: added `getTestNgGroups()`. Design note (discovered mid-implementation): by the time this runs, `TAG` has already been mutated by `getPlatformTagsAndLaunchName()` into a full Cucumber boolean expression (e.g. user's `@calculator` becomes `"@calculator and @cli and not @wip"` — the platform tag and `and not @wip` are auto-injected even when the user supplies nothing). Reading the mutated `TAG` directly would leak Cucumber-only tokens into TestNG groups, so a new `RAW_TAG_BEFORE_CUCUMBER_INFERENCE` config is captured at the top of `getPlatformTagsAndLaunchName()`, before any mutation, and `getTestNgGroups()` reads that instead — giving exactly the tags the user typed, nothing auto-injected. Test-first: `SetupTestNgGroupsTest` (no tag → empty list; single/multiple explicit tags → matching groups; confirms the raw value survives Cucumber's suffix mutation). All green.
- [x] **1.7** No new config needed — reused the existing `PARALLEL` config (already loaded via `getOverriddenIntValue`/`getIntegerValueFromConfigs`, already used to drive Cucumber's own parallel thread count). `TESTNG_PARALLEL_THREAD_COUNT` from the original plan was unnecessary duplication; dropped in favour of reuse.
- [x] **1.8** New `src/test/java/com/znsio/teswiz/testng/InteractiveCalculatorCliTestNgTest.java` — non-data-driven walking-skeleton pilot, direct port of `cli.feature`, calling the existing unmodified `InteractiveCalculatorCLIBL`. **Verified end-to-end**: `CONFIG=configs/cli_local_config.properties FRAMEWORK=testng TAG=@calculator ./gradlew run` → group filtering correctly selected only this pilot, `ConsumerLayerAspectLogging` fired at INFO for every `InteractiveCalculatorCLIBL` method, hooks/driver teardown ran, test passed, process exited 0.
- [x] **1.9** New `src/test/java/com/znsio/teswiz/testng/CryptoApiPriceChangeDataDrivenTestNgTest.java` — `@DataProvider`-based port of `cyptoAPI.feature`'s Scenario Outline, calling the existing unmodified `CryptoAPIBL`, using the Examples table's exact values (symbol + maxPriceChange pairs). **Verified end-to-end**: `CONFIG=configs/api_local_config.properties FRAMEWORK=testng TAG=@cryptoAPI ./gradlew run` → all 4 data rows ran in parallel across distinct threads; all 4 failed with a live `404` from the Binance endpoint — confirmed **pre-existing and unrelated** to this work by running the equivalent Cucumber-mode command (`CONFIG=configs/api_local_config.properties TAG=@cryptoAPI ./gradlew run`, no `FRAMEWORK` override), which fails identically (5/5 scenarios failed, same cause). TestNG mode reproduces Cucumber mode's behavior exactly, which is the actual thing being proven here.
- [x] **1.10** End-to-end verification run — done as part of 1.8/1.9 above. Also re-ran the full `./gradlew test` unit-test suite: same pre-existing failure set as Phase 0's baseline (Playwright browser binaries missing locally, no other regressions) — every new test added in Phase 1 passes.
- [x] **1.11** Updated docs:
  - `docs/guides/ConfiguringTestExecution-README.md` — new "Choosing between Cucumber and plain TestNG (`FRAMEWORK`)" section.
  - `docs/features/ConfigurationParameters-README.md` — **discovered mid-implementation**: this file already documented `FRAMEWORK` as an "ATD property. We will always use cucumber" — a stale historical note. Verified `AppiumTestDistribution` is not an actual dependency anywhere in `build.gradle` or source imports (the jar found under `~/.m2` is unrelated local cache), so the property was safely repurposable; updated the doc line to describe its real, new meaning instead of leaving stale/contradictory documentation in place.
  - `.codex/skills/teswiz-project/SKILL.md` — added the new `com.znsio.teswiz.testng` package to the package-boundaries list and repo map.
  - `CLAUDE.md`/`ANTIGRAVITY.md` — left unchanged; neither enumerates packages or features (they only point at the skill file and hold generic Gradle/CI conventions), so nothing in their existing content was made stale by this change.
- [x] **1.12** Suggested commits ready for manual commit (below).

**Suggested commits (Phase 1, one per step, reflecting what was actually built):**
```
feat(config): wire up existing FRAMEWORK property to select cucumber vs testng mode

FRAMEWORK already existed (unused) in nearly every consumer config
file, defaulting to "cucumber". Add Setup.isTestNgExecutionMode() to
finally read and act on it. Default behavior is unchanged for every
existing consumer.
```
```
build(deps): declare TestNG as a direct dependency

Previously only available transitively via cucumber-testng; declare
directly (7.12.0, the version already resolved) so TestNG-mode code
is independent of the Cucumber dependency chain.
```
```
feat(runner): add Drivers.attachLogsAndCloseAllDrivers(boolean) decoupled from Cucumber Status

Extract the pass/fail decision out of Cucumber's Status enum so
driver teardown and cloud status reporting work identically whether
called from Cucumber's Scenario or a plain boolean.
```
```
feat(testng): add TestNgTestExecutionContextFactory for per-test directory scaffolding

Cucumber mode gets its SCREENSHOT_DIRECTORY/SCENARIO_LOG_DIRECTORY/etc.
from CucumberScenarioListener before Hooks ever runs; TestNG mode had
no equivalent, so constructing a real ScreenShotManager crashed with
"Directory is null or empty". This factory mirrors that setup,
reusing the same FileUtils/OsUtils/StringUtils/FileLocations
utilities Cucumber mode already uses.
```
```
feat(testng): add programmatic TestNgRunner and TeswizTestNgListener

TestNgRunner wraps org.testng.TestNG configured entirely in code (no
testng.xml, consistent with this repo's existing XML-free Cucumber/
TestNG setup) with parallel mode and thread count from config.
TeswizTestNgListener wires TestNG's ITestListener lifecycle to
TestNgTestExecutionContextFactory and the new Hooks overloads,
mirroring what RunCukes' @Before/@After do for Cucumber mode.
```
```
feat(steps): add Hooks overloads decoupled from Cucumber Scenario

beforeScenario(String)/afterScenario(String, boolean) let Hooks'
setup/teardown logic be shared between Cucumber and TestNG lifecycles
without duplicating logic. Existing Scenario-based overloads delegate
to these and are unchanged in behavior.
```
```
feat(runner): fork Runner.run on FRAMEWORK to support TestNG-only execution mode

Runner now dispatches to the existing Cucumber CLI path or the new
TestNgRunner path based on Setup.isTestNgExecutionMode(), using a
fixed, hardcoded pilot test class list (no dynamic discovery yet -
tracked in Backlog). Cucumber mode behavior is unchanged.
```
```
feat(runner): map TAG config to simple TestNG include groups

Add Setup.getTestNgGroups(), reading a new RAW_TAG_BEFORE_CUCUMBER_INFERENCE
config captured before Cucumber's tag-expression inference mutates
TAG, so TestNG groups reflect exactly what the user typed. Boolean
tag-expression parity (and/not) is intentionally deferred - tracked
as a follow-up, not built here.
```
```
test(testng): add TestNG walking-skeleton test for interactive calculator CLI

Ports cli.feature's scenario to a plain TestNG test calling the
existing InteractiveCalculatorCLIBL unchanged. Verified end-to-end via
`FRAMEWORK=testng TAG=@calculator ./gradlew run` - group filtering,
hooks, aspect logging, and driver teardown all work.
```
```
test(testng): add data-driven TestNG test for crypto API price-change validation

Ports cyptoAPI.feature's Scenario Outline to a parallel
@DataProvider-backed TestNG test against the existing CryptoAPIBL
unchanged. Verified end-to-end: fails identically to the Cucumber
version against the same (currently broken) live endpoint, confirming
TestNG mode reproduces Cucumber mode's behavior exactly.
```
```
docs: document FRAMEWORK config property and TestNG-only execution mode
```

---

## Verification

**Phase 0:**
```bash
./gradlew test --tests "com.znsio.teswiz.aspect.AspectLoggingWeavingTest"
./gradlew test
```

**Phase 1:**
```bash
./gradlew test --tests "com.znsio.teswiz.runner.SetupFrameworkModeTest"
./gradlew test --tests "com.znsio.teswiz.runner.SetupTestNgGroupsTest"
./gradlew test --tests "com.znsio.teswiz.testng.TestNgRunnerTest"
./gradlew test --tests "com.znsio.teswiz.steps.HooksTestNgOverloadTest"
```
End-to-end via the existing `run` task:
```bash
CONFIG=configs/cli_local_config.properties FRAMEWORK=testng TAG=@calculator ./gradlew run
```
Success criteria: log4j output shows the pilot and data-driven tests executing (data-driven test running 4 times, once per crypto symbol), evidence of more than one thread ID (parallelism), `TeswizTestNgListener` firing `Hooks` setup/teardown around each test, process exit status correctly reflecting pass/fail, no ReportPortal calls in this phase.

**Phase 2:**
```bash
./gradlew test --tests "com.znsio.teswiz.testng.TestNgTestClassDiscoveryTest"
```
Same end-to-end command as Phase 1 — now backed by discovery instead of a hardcoded list.

---

## Phase 2 — Dynamic test-class discovery (Cucumber `--glue` equivalent)

**Goal**: replace Phase 1's hardcoded two-class pilot list in `Runner.runTestNgMode(...)` with classpath scanning, so TestNG mode can run a consumer's actual test suite, not just the walking-skeleton pilots.

Decisions (confirmed):
- Scanning mechanism: **`org.reflections:reflections:0.10.2`** (new dependency) rather than a hand-rolled scanner — well-established, purpose-built for exactly this, handles jar/classpath edge cases we'd otherwise have to get right ourselves.
- Scan-package config: **reuses the existing `Runner` constructor's second arg** (`args[1]`, today the Cucumber `--glue` package, e.g. `com/znsio/teswiz/steps`) — no new config surface. When `FRAMEWORK=testng`, this same arg is reinterpreted as the TestNG scan package (slash-to-dot converted). teswiz's own `build.gradle` `run` task now branches this value on `FRAMEWORK` (`com/znsio/teswiz/testng` for TestNG mode, unchanged `com/znsio/teswiz/steps` for Cucumber mode) purely for its own dogfooding runs — a real consumer passes their own package via their own invocation.

- [x] Added `org.reflections` dependency (`build.gradle`).
- [x] New `src/main/java/com/znsio/teswiz/testng/TestNgTestClassDiscovery.java` — `discoverTestClassesIn(String packageName)` returns every class with at least one `@org.testng.annotations.Test`-annotated method, found via `Reflections(packageName, Scanners.MethodsAnnotated)`. Test-first: `TestNgTestClassDiscoveryTest` (discovers the 3 existing `testng.fixtures` classes; returns empty for a package with none). Green.
- [x] `Runner.runTestNgMode(...)` now takes the scan-package arg, converts `/` to `.`, calls the discovery class instead of the removed `TESTNG_PILOT_TEST_CLASSES` constant.
- [x] `build.gradle`'s `run` task branches the scan-package arg on `FRAMEWORK` for teswiz's own dogfood runs.
- [x] **Verified end-to-end**, same command as Phase 1 (`FRAMEWORK=testng TAG=@calculator ./gradlew run`): log confirms `"Begin running 5 TestNG test class(es) discovered in package 'com.znsio.teswiz.testng'"` (the 2 real pilots + the 3 `fixtures` classes used by `TestNgRunnerTest`/`TestNgTestClassDiscoveryTest`), but group filtering correctly narrowed execution to just the 1 matching test (`Total tests run: 1, Passes: 1`) — same result as Phase 1's hardcoded-list run.
- [x] Full `./gradlew test` suite re-run: same pre-existing failure family as Phase 0/1's baseline (Playwright browser binaries missing locally, one network timeout) — no new regressions.

**Known characteristic, not a bug**: `Reflections` scans a package **and all its subpackages**, so scanning `com.znsio.teswiz.testng` also picks up `com.znsio.teswiz.testng.fixtures` (teswiz's own test-support fixtures, tagged `groups = "fixture"`). Group-based `TAG` filtering keeps this harmless in practice (fixtures never match a real group unless someone explicitly runs `TAG=@fixture`), but it does mean "discovered" counts include incidental fixture/support classes living under the scanned package tree — worth keeping test-only fixtures out of a consumer's main scan package if that noise matters to them.

### Suggested commits (Phase 2)

```
feat(testng): add org.reflections-based TestNG test-class discovery

Add TestNgTestClassDiscovery.discoverTestClassesIn(package), scanning
for @Test-annotated methods via org.reflections. Replaces Phase 1's
hardcoded two-class pilot list, letting TestNG mode run a consumer's
actual test suite.
```
```
feat(runner): wire dynamic test-class discovery into Runner.runTestNgMode

Reinterpret the existing Cucumber --glue package arg as the TestNG
scan package when FRAMEWORK=testng (slash-to-dot converted) - no new
config surface. build.gradle's run task branches its own dogfood-run
scan package on FRAMEWORK accordingly.
```

---

## Backlog (deferred, not forgotten — not part of this plan's scope)

- Support Cucumber-style boolean tag expressions (`and`/`not`) when mapping `TAG` config to TestNG include/exclude groups (Phase 1 only does simple inclusion).
- ReportPortal step-level logging for TestNG mode (likely via `agent-java-testng` + extending the Phase-0-aligned `src/test` `AspectLogging` aspect to also call `ReportPortalLogger`).
- Tag-coverage HTML report equivalent to the Cucumber/masterthought one, for TestNG mode.
- One-time Cucumber → TestNG migration tooling (skill/agent) for existing consumers who later want to switch.

## Open items

- Exact TestNG version to pin as a **direct** dependency (1.2) — defaulting to `7.12.0` (currently resolved transitively) unless a newer version is requested.
