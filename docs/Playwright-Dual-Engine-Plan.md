# teswiz Dual-Engine Web Plan

This checklist tracks the remaining implementation for the dual-engine web architecture.

## Current State

- [x] `WEB_ENGINE` is explicit in checked-in configs and report metadata
- [x] Playwright TS local single-user web runs work
- [x] Playwright TS local multi-user web runs work
- [x] Playwright TS local mixed Android + web runs work
- [x] TheApp invalid-login web flow uses first-class `playwright-ts` screen execution instead of Selenium web screens
- [x] TheApp invalid-login Playwright path calls real worker-side TypeScript screen modules
- [x] Google Search web flow uses real worker-side TypeScript screen modules instead of Selenium web screens
- [x] Indigo web contracts use real worker-side TypeScript screen modules instead of Selenium web screens
- [x] Jiomeet web contracts use real worker-side TypeScript screen modules instead of Selenium web screens
- [x] Shared `ScreenShotScreen` web contract uses a real worker-side TypeScript screen module
- [x] TheApp local single-user Playwright-TS validation passes through the first-class screen path
- [x] TheApp local multi-user Playwright-TS validation passes through the first-class screen path
- [x] TheApp local single-user Playwright-Java validation passes through the first-class screen path
- [x] TheApp local multi-user Playwright-Java validation passes through the first-class screen path
- [x] TheApp local mixed Android + Playwright-Java validation passes through the first-class screen path
- [x] Google Search web contracts now have explicit `playwright-java` screen implementations
- [x] TheApp file-upload web contract now has an explicit `playwright-java` screen implementation
- [x] Android-only TheApp web actions now fail explicitly and consistently across Selenium, Playwright-Java, and Playwright-TS
- [x] Browser-config migration reporting and visible guidance are implemented
- [x] Playwright-Java trace, HAR, and console artifacts flow through the shared scenario artifact reporter
- [x] Playwright BrowserStack and LambdaTest session metadata is normalized into shared session/report metadata
- [x] Architecture docs exist for the current web-engine model
- [x] Repo guidance files are aligned for Codex, Claude, and Antigravity
- [x] Browser orchestration lives in `com.znsio.teswiz.web.browser`
- [x] Selenium web runtime helpers live in `com.znsio.teswiz.web.selenium`
- [x] Selenium web cloud setup entry points live in `com.znsio.teswiz.web.provider.selenium`
- [x] Web engine packages return a small session/result object and `runner` owns web `Driver` construction
- [x] Web session creation no longer depends on hidden `ENGINE_SESSION_HANDLE` context side effects
- [x] Playwright worker tests no longer rely on subclassing concrete resolver classes as extension points
- [x] Browser shutdown routing follows the configured web engine
- [x] Focused browser-routing and web-engine tests pass
- [x] Screen contracts can resolve implementations through centralized `ScreenRegistry.getScreen(...)`
- [x] User-facing screen runtime and screen verification/reporting infrastructure now ship from `src/main`
- [x] Framework-owned Playwright TS module bridging removes the need for test-side Java adapter classes when a matching `.ts` screen module exists
- [x] `playwright-ts` screen resolution now fails explicitly on missing `.ts` modules instead of falling through to generic Java implementation resolution
- [x] Runtime coverage proves missing `playwright-ts` business-screen modules fail with the expected module path
- [x] PDF validation now lives directly in the main-side visual framework instead of test-side screen overrides

## Milestone 1: Normalize package boundaries and reduce public surface area

- [x] Move browser orchestration fully out of `runner`
- [x] Keep `BrowserDriverManager` in `com.znsio.teswiz.web.browser` as the browser-engine orchestrator
- [x] Keep `SeleniumDriverManager` in `com.znsio.teswiz.web.selenium` as the Selenium runtime implementation
- [x] Keep engine/provider namespaces consistent for web and mobile
- [x] Align cloud capability helpers under provider namespaces
- [x] Introduce a small public engine session/result API so `runner` can keep owning `Driver` construction while engine packages remain internal
- [x] Re-run focused compile/test verification after each package move to keep the refactor mechanically safe
- [x] Move user-facing screen runtime and screen validation/reporting infrastructure out of `src/test` and into `src/main`
- [ ] Replace broad public exposure with small facades or result objects where needed
- [ ] Move remaining Selenium-web runtime helpers into `com.znsio.teswiz.web.selenium`
- [ ] Keep web and mobile provider namespaces symmetrical
- [ ] Keep the package surface small enough that engine internals stay easy to reason about

## Milestone 2: Add first-class Playwright-engine screen implementations

- [x] Model `playwright-java` as an explicit web engine in core engine selection, screen-target naming, and browser-manager routing
- [ ] Create explicit Java screen implementations for the Playwright-Java web engine for the shared business contract, starting with TheApp flows
- [x] Create framework-owned `playwright-ts` module bridging for the shared business contract so users only implement the contract and matching `.ts` module
- [ ] Retire the current Selenium-screen-through-Playwright compatibility path once equivalent first-class Java screen implementations for Playwright engines are in place
- [ ] Remove reliance on automatic Selenium-to-Playwright mapping where Java screen implementations for Playwright engines exist
- [x] Route screen creation via config-driven screen factories for TheApp web screens
- [x] Add first-class `playwright-ts` screen execution for the TheApp invalid-login flow
- [x] Add the first real worker-side `.ts` screen modules and bridge them through the Java screen contract for TheApp invalid-login
- [x] Remove test-side Java adapter classes where framework-owned `playwright-ts` screen bridging is available
- [x] Add first-class `playwright-ts` screen-module execution for the TheApp file-upload flow
- [x] Centralize contract-to-implementation resolution so screen `get()` methods stay one-line and non-breaking
- [x] Add screen contract compliance checks for supported platform/engine combinations
- [x] Add local single-user Selenium/Playwright parity tests for the shared screen contract
- [x] Replace the temporary `playwright-java` fail-fast runtime seam with a real Playwright-Java browser session implementation
- [ ] Keep Selenium screens working exactly as before while adding Java screen implementations for Playwright engines

## Milestone 3: Add explicit Gradle contract sanity check

- [x] Implement a user-invokable Gradle task to validate screen contracts and report non-compliant implementations
- [x] Ensure the sanity-check task fails with a readable list of missing or mismatched screen methods per platform and engine
- [x] Make the task explicit so users can run it only when they want contract validation
- [x] Include a report of unsupported or partially implemented screen combinations

## Milestone 4: Add missing-contract generation task

- [x] Implement a separate Gradle command to enumerate missing contracts across supported platforms
- [x] Keep contract-generation intentionally separate from normal builds so it remains an explicit maintenance command
- [x] Make the output suitable for a human-readable gap report
- [x] Keep this command opt-in and non-blocking for normal test runs

## Milestone 5: Expand multi-user and multi-platform coverage

- [ ] Validate mixed persona flows across Selenium, Playwright-Java, Playwright-TS, and Appium combinations
- [ ] Preserve persona/session routing, concurrent ownership, and mixed platform scenarios as engines multiply
- [ ] Add at least one web-web multi-user scenario per supported web engine
- [ ] Add at least one mixed web-mobile scenario per supported mobile platform
- [ ] Verify persona switching and cleanup behavior under both single-user and multi-user runs
- [x] Validate a local `playwright-ts` single-user web scenario end to end (`@theapp2 and @invalidLogin1 and @playwright-phase1`)
- [x] Validate a local `playwright-ts` web-web multi-user scenario end to end (`@multiuser-web and @theapp7 and @playwright-phase1`)
- [x] Validate a local Android + `playwright-ts` web mixed scenario end to end (`@multiuser-android-web and @theapp5 and @playwright-phase1`)
- [x] Validate a local `playwright-java` single-user web scenario end to end (`@theapp2 and @invalidLogin1`)
- [x] Validate a local `playwright-java` web-web multi-user scenario end to end (`@multiuser-web and @theapp7`)
- [x] Validate a local Android + `playwright-java` web mixed scenario end to end (`@multiuser-android-web and @theapp5`)

## Milestone 6: Visual and reporting parity

- [x] Keep Applitools, ReportPortal, HTML reports, logs, and artifact attachment behavior consistent across all web engines
- [x] Publish explicit Playwright artifact filenames in shared session metadata and use them before legacy naming fallbacks during report attachment
- [x] Confirm Playwright-specific visual, trace, console, and HAR artifacts flow through the same reporting contract
- [x] Route Selenium browser logs and Appium device logs through the shared scenario artifact publishing path
- [x] Aggregate normalized provider-native artifact URLs into shared Cucumber HTML report metadata
- [x] Add a Playwright-specific visual adapter path
- [x] Keep Selenium Eyes behavior unchanged
- [x] Ensure engine, provider, platform, persona, and session metadata are visible in reports
- [x] Aggregate provider-native cloud session ids and report URLs into the cucumber HTML report metadata
- [x] Emit provider-native web cloud report links into ReportPortal through the shared scenario reporting path

## Milestone 7: Cloud execution parity

- [x] Add a shared Playwright provider-config seam for BrowserStack, LambdaTest, HeadSpin, and local execution so both Playwright engines receive normalized provider metadata and endpoints
- [x] Add the first real `playwright-ts` provider adapter at the worker boundary for local execution and BrowserStack websocket connection shaping
- [x] Add the LambdaTest `playwright-ts` worker adapter using the current teswiz web capability shape
- [x] Add Playwright-Java remote connection resolution for BrowserStack and LambdaTest using the same normalized provider seam
- [x] Fail fast with an explicit HeadSpin-not-supported error when Playwright web engines are used with HeadSpin config
- [x] Route BrowserStack and LambdaTest session-name/status commands through Playwright cloud-control script paths for both Playwright engines
- [x] Normalize BrowserStack and LambdaTest provider session/report artifact URLs into Playwright session metadata
- [x] Validate Selenium web session-handle creation against the shared cloud metadata normalization path for LambdaTest and BrowserStack
- [ ] Add or complete provider-specific Playwright web adapters for BrowserStack, LambdaTest, and local CI
- [x] Move web cloud provider-config parsing into the shared `web.provider` boundary
- [x] Move web cloud session-name/status command shaping into the shared `web.provider` boundary
- [x] Move Selenium remote URL/capability request shaping into `web.provider.selenium`
- [ ] Preserve Selenium cloud behavior unchanged while adding Playwright cloud support
- [x] Normalize cloud metadata and artifacts across engines and providers
- [ ] Keep provider-specific setup hidden from business tests

## Milestone 8: Documentation and upgrade guidance

- [ ] Keep README, architecture notes, config docs, and repo skills aligned with the final package and engine model
- [ ] Document any explicit user migration steps, if they are needed
- [ ] Keep the plan file itself updated as milestones change

## Package cleanup progress

- [x] Extract mobile device-session state behind `mobile.session` while keeping `runner` compatibility delegates
- [x] Extract local mobile device and simulator setup behind `mobile.device` while keeping `runner.LocalDevicesSetup` as a compatibility facade
- [x] Extract Appium server lifecycle behind `mobile.server` while keeping `runner.AppiumServerManager` as a compatibility facade
- [x] Extract mobile cloud setup and cleanup routing behind `mobile.provider` while keeping `runner.DeviceSetup` as the stable orchestration delegate
- [x] Extract shared app artifact path resolution behind `config.app` while keeping `runner.DeviceSetup` as the stable orchestration delegate
- [x] Extract shared app version detection behind `config.app` while keeping `runner.DeviceSetup` as the stable orchestration delegate
- [x] Extract shared capability lookup and device-farm capability-file persistence behind `config.capability` while keeping `runner.DeviceSetup` as the stable orchestration delegate

## Notes

- The goal is to keep Selenium Java stable while first-class Playwright-Java and `playwright-ts` support grows.
- For `playwright-ts`, the intended user model is: Java screen contract in `src/test`, matching `.ts` module under `src/test/resources/playwright/screens`, and no extra Java adapter class.
- Engine and provider namespaces should stay separate and easy to infer.
- HeadSpin is intentionally out of scope for Playwright web support in teswiz. If selected with `playwright-java` or `playwright-ts`, teswiz should fail immediately with an explicit unsupported message.
- The compatibility adapter path is useful only as long as it helps bridge the transition to first-class Java screen implementations for Playwright engines.
- This file should be updated whenever a milestone changes state so it remains the live implementation checklist.
