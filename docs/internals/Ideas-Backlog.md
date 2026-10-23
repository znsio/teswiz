# Ideas Backlog

Captured ideas for future teswiz exploration.

## Open Ideas

- Analytics for teswiz usage
  - Track how teswiz is being used across runs, platforms, engines, and CI environments.
  - Useful questions: what gets executed most often, where failures happen, and which features are adopted vs. ignored.

- OCR and image recognition
  - Explore OCR support for extracting text from screenshots, PDFs, or visual artifacts in web and native apps.
  - Explore image recognition for locating and interacting with UI elements such as a spinner icon or other visual controls.
  - Keep Applitools as the validation layer for image comparison and visual testing.

- API test support using Playwright
  - Explore using Playwright for API-level test support alongside the existing web and mobile flows.
  - Consider how API checks could reuse teswiz configuration, reporting, and execution patterns.

## High-Level Plan

### 1. Shared foundations

- Define a cross-platform runtime model that works on Windows, macOS, and Linux first.
- Keep feature entry points consistent with the current teswiz runner, config, and reporting patterns.
- Design for restricted and firewalled networks by avoiding mandatory public-cloud dependencies.
- Make external integrations optional, configurable, and replaceable with local or self-hosted alternatives.
- Prefer offline-friendly workflows where artifacts can be generated locally and synchronized later.

### 2. Usage analytics

- Start with local-first telemetry capture that records run metadata, engine/platform choice, feature usage, and failure categories.
- Store data in a portable format such as JSON or CSV so it can be inspected without extra services.
- Add optional export sinks later for teams that want dashboards, aggregation, or remote analytics.
- Ensure analytics collection can be disabled completely and does not block test execution if unavailable.

### 3. OCR and image recognition

- Introduce OCR as an opt-in capability for web and native app artifacts such as screenshots, images, and PDFs.
- Keep the OCR implementation cross-platform by relying on portable engines or service abstractions rather than OS-specific UI tooling.
- Add image recognition as an interaction layer so tests can target visual controls without using it for visual validation.
- Support both local execution and constrained-network execution by allowing offline engines or preconfigured local services.

### 4. Playwright API test support

- Add an API-testing path that fits alongside the existing Playwright and runner architecture instead of becoming a separate framework.
- Reuse teswiz configuration, secrets handling, reporting, and execution lifecycle where practical.
- Keep the API layer usable on all supported desktop operating systems with no platform-specific behavior in the core flow.
- Support restricted networks by allowing base URLs, proxies, certificates, and mocks/stubs to be configured locally.

### 5. Validation and rollout

- Add focused tests for each new capability before widening the surface area.
- Validate the same feature set on Windows, macOS, and Linux before calling the work complete.
- Prefer incremental delivery so each capability can ship independently and remain usable on its own.
- Document any new setup requirements, especially for offline mode, proxies, certificates, or self-hosted services.

## Notes

- These items are intentionally unscoped for now.
- Add implementation sketches, links, or owners here as the ideas become more concrete.
