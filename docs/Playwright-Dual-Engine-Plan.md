# teswiz Dual-Engine Web Plan

This checklist tracks the remaining implementation for the dual-engine web architecture.

## Current State

- [x] `WEB_ENGINE` is explicit in checked-in configs and report metadata
- [x] Playwright TS local single-user web runs work
- [x] Playwright TS local multi-user web runs work
- [x] Playwright TS local mixed Android + web runs work
- [x] Browser-config migration reporting and visible guidance are implemented
- [x] Architecture docs exist for the current web-engine model

## Milestone 1: Normalize package boundaries and reduce public surface area

- [ ] Move browser orchestration fully out of `runner`
- [ ] Keep `BrowserDriverManager` in `com.znsio.teswiz.web.browser` as the browser-engine orchestrator
- [ ] Keep `SeleniumDriverManager` in `com.znsio.teswiz.web.selenium` as the Selenium runtime implementation
- [ ] Keep engine/provider namespaces consistent for web and mobile
- [ ] Replace broad public exposure with small facades or result objects where needed
- [ ] Introduce a small public engine session/result API so `runner` can keep owning `Driver` construction while engine packages remain internal
- [ ] Move remaining Selenium-web runtime helpers into `com.znsio.teswiz.web.selenium`
- [ ] Align cloud capability helpers under provider namespaces
- [ ] Re-run focused compile/test verification after each package move to keep the refactor mechanically safe

## Milestone 2: Add first-class Playwright screens

- [ ] Create explicit Playwright-Java and Playwright-TS screen implementations for the shared business contract, starting with TheApp flows
- [ ] Retire the current Selenium-screen-through-Playwright compatibility path once equivalent first-class Playwright screens are in place
- [ ] Remove reliance on automatic Selenium-to-Playwright mapping where Playwright screens exist
- [ ] Route screen creation via config-driven screen factories
- [ ] Add screen contract compliance checks for supported platform/engine combinations

## Milestone 3: Add explicit Gradle contract sanity check

- [ ] Implement a user-invokable Gradle task to validate screen contracts and report non-compliant implementations
- [ ] Ensure the sanity-check task fails with a readable list of missing or mismatched screen methods per platform and engine

## Milestone 4: Add missing-contract generation task

- [ ] Implement a separate Gradle command to enumerate or scaffold missing contracts across supported platforms
- [ ] Keep contract-generation intentionally separate from normal builds so it remains an explicit maintenance command

## Milestone 5: Expand multi-user and multi-platform coverage

- [ ] Validate mixed persona flows across Selenium, Playwright-Java, Playwright-TS, and Appium combinations
- [ ] Preserve persona/session routing, concurrent ownership, and mixed platform scenarios as engines multiply

## Milestone 6: Visual and reporting parity

- [ ] Keep Applitools, ReportPortal, HTML reports, logs, and artifact attachment behavior consistent across all web engines
- [ ] Confirm Playwright-specific visual, trace, console, and HAR artifacts flow through the same reporting contract

## Milestone 7: Cloud execution parity

- [ ] Add or complete provider-specific Playwright web adapters for BrowserStack, LambdaTest, HeadSpin, and local CI
- [ ] Keep provider logic at the boundary
- [ ] Preserve Selenium cloud behavior unchanged while adding Playwright cloud support

## Milestone 8: Documentation and upgrade guidance

- [ ] Keep README, architecture notes, config docs, and repo skills aligned with the final package and engine model

## Notes

- The goal is to keep Selenium Java stable while first-class Playwright-Java and Playwright-TS support grows.
- Engine and provider namespaces should stay separate and easy to infer.
- The compatibility adapter path is useful only as long as it helps bridge the transition to first-class Playwright screens.
