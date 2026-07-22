# teswiz Dual-Engine Web Plan

This checklist tracks the remaining implementation for the dual-engine web architecture.

## Current State

- [x] `WEB_ENGINE` is explicit in checked-in configs and report metadata
- [x] Playwright TS local single-user web runs work
- [x] Playwright TS local multi-user web runs work
- [x] Playwright TS local mixed Android + web runs work
- [x] Browser-config migration reporting and visible guidance are implemented
- [x] Architecture docs exist for the current web-engine model
- [x] Repo guidance files are aligned for Codex, Claude, and Antigravity
- [x] Browser orchestration lives in `com.znsio.teswiz.web.browser`
- [x] Selenium web runtime helpers live in `com.znsio.teswiz.web.selenium`
- [x] Browser shutdown routing follows the configured web engine
- [x] Focused browser-routing and web-engine tests pass

## Milestone 1: Normalize package boundaries and reduce public surface area

- [x] Move browser orchestration fully out of `runner`
- [x] Keep `BrowserDriverManager` in `com.znsio.teswiz.web.browser` as the browser-engine orchestrator
- [x] Keep `SeleniumDriverManager` in `com.znsio.teswiz.web.selenium` as the Selenium runtime implementation
- [x] Keep engine/provider namespaces consistent for web and mobile
- [x] Re-run focused compile/test verification after each package move to keep the refactor mechanically safe
- [ ] Replace broad public exposure with small facades or result objects where needed
- [ ] Introduce a small public engine session/result API so `runner` can keep owning `Driver` construction while engine packages remain internal
- [ ] Move remaining Selenium-web runtime helpers into `com.znsio.teswiz.web.selenium`
- [ ] Align cloud capability helpers under provider namespaces
- [ ] Keep web and mobile provider namespaces symmetrical
- [ ] Keep the package surface small enough that engine internals stay easy to reason about

## Milestone 2: Add first-class Playwright screens

- [ ] Create explicit Playwright-Java and Playwright-TS screen implementations for the shared business contract, starting with TheApp flows
- [ ] Retire the current Selenium-screen-through-Playwright compatibility path once equivalent first-class Playwright screens are in place
- [ ] Remove reliance on automatic Selenium-to-Playwright mapping where Playwright screens exist
- [ ] Route screen creation via config-driven screen factories
- [ ] Add screen contract compliance checks for supported platform/engine combinations
- [ ] Add local single-user Selenium/Playwright parity tests for the shared screen contract
- [ ] Keep Selenium screens working exactly as before while adding Playwright screen implementations

## Milestone 3: Add explicit Gradle contract sanity check

- [ ] Implement a user-invokable Gradle task to validate screen contracts and report non-compliant implementations
- [ ] Ensure the sanity-check task fails with a readable list of missing or mismatched screen methods per platform and engine
- [ ] Make the task explicit so users can run it only when they want contract validation
- [ ] Include a report of unsupported or partially implemented screen combinations

## Milestone 4: Add missing-contract generation task

- [ ] Implement a separate Gradle command to enumerate or scaffold missing contracts across supported platforms
- [ ] Keep contract-generation intentionally separate from normal builds so it remains an explicit maintenance command
- [ ] Make the output suitable for either scaffolding or a human-readable gap report
- [ ] Keep this command opt-in and non-blocking for normal test runs

## Milestone 5: Expand multi-user and multi-platform coverage

- [ ] Validate mixed persona flows across Selenium, Playwright-Java, Playwright-TS, and Appium combinations
- [ ] Preserve persona/session routing, concurrent ownership, and mixed platform scenarios as engines multiply
- [ ] Add at least one web-web multi-user scenario per supported web engine
- [ ] Add at least one mixed web-mobile scenario per supported mobile platform
- [ ] Verify persona switching and cleanup behavior under both single-user and multi-user runs

## Milestone 6: Visual and reporting parity

- [ ] Keep Applitools, ReportPortal, HTML reports, logs, and artifact attachment behavior consistent across all web engines
- [ ] Confirm Playwright-specific visual, trace, console, and HAR artifacts flow through the same reporting contract
- [ ] Add a Playwright-specific visual adapter path
- [ ] Keep Selenium Eyes behavior unchanged
- [ ] Ensure engine, provider, platform, persona, and session metadata are visible in reports

## Milestone 7: Cloud execution parity

- [ ] Add or complete provider-specific Playwright web adapters for BrowserStack, LambdaTest, HeadSpin, and local CI
- [ ] Keep provider logic at the boundary
- [ ] Preserve Selenium cloud behavior unchanged while adding Playwright cloud support
- [ ] Normalize cloud metadata and artifacts across engines and providers
- [ ] Keep provider-specific setup hidden from business tests

## Milestone 8: Documentation and upgrade guidance

- [ ] Keep README, architecture notes, config docs, and repo skills aligned with the final package and engine model
- [ ] Document any explicit user migration steps, if they are needed
- [ ] Keep the plan file itself updated as milestones change

## Notes

- The goal is to keep Selenium Java stable while first-class Playwright-Java and Playwright-TS support grows.
- Engine and provider namespaces should stay separate and easy to infer.
- The compatibility adapter path is useful only as long as it helps bridge the transition to first-class Playwright screens.
- This file should be updated whenever a milestone changes state so it remains the live implementation checklist.
