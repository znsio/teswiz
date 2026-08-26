# Cucumber to TestNG Migration Guide

teswiz supports two execution modes, selected via the `FRAMEWORK` config property (see
[ConfiguringTestExecution-README.md](../guides/ConfiguringTestExecution-README.md)):

* `FRAMEWORK=cucumber` (default) - `.feature` files → step definitions → business/screen layers.
* `FRAMEWORK=testng` - plain TestNG `@Test` classes call the same business/screen layers directly,
  skipping the Gherkin/step-definition translation layer entirely.

A single execution runs only one mode - there is no coexistence within one run. This guide is for
consumers who have existing Cucumber feature files and want to port some or all of them to TestNG mode.

There is currently **no automated migration tool** - this is a manual, one-scenario-at-a-time process.
This guide documents the approach so it's consistent and predictable, whether you're doing it by hand or
scripting your own generator against these same rules.

---

## The core idea

A Cucumber scenario is really just an ordered list of step-definition method calls, and each step-def
method body is itself just calls into `Runner`/`Drivers`/business-layer classes - it doesn't do anything
Cucumber-specific beyond translating Gherkin text into those calls. Migrating a scenario means replaying
that same call sequence directly in a TestNG `@Test` method, in the same order, calling the same
business-layer/screen classes, unchanged.

Concretely, from the existing `theapp.feature`:

```gherkin
@android @web @iOS @headspin @browserstack @lambdatest @invalidLogin @invalidLogin1 @theapp2 @playwright-phase1
Scenario: Verify error message on invalid login
  Given I login with invalid credentials - "znsio1", "invalid password"
```

whose step definition (`TheAppSteps.iLoginWithInvalidCredentials`) is:

```java
@When("I login with invalid credentials - {string}, {string}")
public void iLoginWithInvalidCredentials(String username, String password) {
    context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, "./configs/browser_config.json");
    context.addTestState(TEST_CONTEXT.UPDATED_BASE_URL_FOR_WEB, "BASE_URL");
    Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
    context.addTestState(SAMPLE_TEST_CONTEXT.ME, username);
    new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).provideInvalidDetailsForSignup(username, password);
}
```

becomes this TestNG test (the actual pilot already shipped in this repo, at
[`TheAppInvalidLoginTestNgTest`](../../src/test/java/com/znsio/teswiz/testng/TheAppInvalidLoginTestNgTest.java)):

```java
public class TheAppInvalidLoginTestNgTest {
    private static final String USERNAME = "znsio1";
    private static final String PASSWORD = "invalid password";

    @Test(groups = {"web", "theapp"})
    public void verifyErrorMessageOnInvalidLogin() {
        createDriverForInvalidLoginAttempt();

        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform())
                .provideInvalidDetailsForSignup(USERNAME, PASSWORD);
    }

    private void createDriverForInvalidLoginAttempt() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, "./configs/browser_config.json");
        context.addTestState(TEST_CONTEXT.UPDATED_BASE_URL_FOR_WEB, "BASE_URL");
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, USERNAME);
    }
}
```

Note what changed and what didn't:

* The literal argument strings (`"znsio1"`, `"invalid password"`) become named constants.
* The step-def's driver/context bootstrapping is hoisted into a private `createXxx()` helper, separating
  **setup** from the **actual test flow** - keep the `@Test` method itself reading as the scenario's
  narrative, one BL call at a time.
* `new AppBL(...).provideInvalidDetailsForSignup(...)` is copied verbatim - the business layer is never
  touched or reimplemented.
* The Gherkin tags become the TestNG `@Test(groups = {...})` array (see [Tags → groups](#tags--testng-groups) below).

---

## Step-by-step process

For each scenario you want to migrate:

1. **Find the scenario's step-def methods.** Match each step's text (`Given`/`When`/`Then`/`And`) to its
   `@Given`/`@When`/`@Then`/`@And`-annotated method in the relevant `*Steps.java` class.
2. **Copy each step-def method's body, in order**, into the new TestNG test method - substituting the
   scenario's literal argument values (or `Examples` row values, for outlines) for the step-def's method
   parameters.
3. **Separate setup from test flow.** Where a step-def does driver/context bootstrapping before calling a
   BL method (as above), pull that bootstrapping into a private helper method called first, so the
   `@Test` method's body is just the sequence of BL calls.
4. **Chain BL calls where the BL supports it.** Several BL classes return the next BL type from a method
   (e.g. `AuthBL.signIn()` returns `LandingBL`, `LandingBL.startInstantMeeting()` returns `InAMeetingBL`) -
   use that chaining instead of constructing a fresh BL instance per call. Read the actual BL source before
   assuming a method's return type; don't guess.
5. **Map `Background:` steps** into the same setup helper (or a shared one, if migrating a whole feature
   file into one test class with multiple `@Test` methods).
6. **Map tags to TestNG groups** - see below.
7. **Map `Scenario Outline`/`Examples`** to a TestNG `@DataProvider` - see below.
8. **Run it, both ways, and compare.** Run the original Cucumber scenario and the new TestNG test against
   the same config/tag, and confirm they produce the same pass/fail outcome. Don't assume equivalence -
   verify it.

---

## Tags → TestNG groups

Cucumber tags on a scenario become the `@Test(groups = {...})` array. `TAG` filtering works identically in
both modes (see [ConfiguringTestExecution-README.md](../guides/ConfiguringTestExecution-README.md)) - the
same `and`/`or`/`not` syntax you already use for `TAG` is what `TestNgTagExpressionParser` translates into
TestNG's include/exclude groups at runtime.

One real limitation to design around: TestNG's group model can express "belongs to any included group" and
"belongs to no excluded group," but **not** "belongs to two groups at once" (a true AND of two positive
tags). If a Cucumber scenario relies on `TAG="@schedule and @signup"` semantics, give the migrated TestNG
test a single composite group instead (e.g. `@Test(groups = "scheduleAndSignup")`) rather than trying to
express the AND at select-time.

## Scenario Outline / Examples → `@DataProvider`

A `Scenario Outline` with an `Examples` table maps directly to a TestNG `@DataProvider`. From
`cyptoAPI.feature`:

```gherkin
Scenario Outline: Validate price change in last 24 hrs for crypto currency <symbol>
  Given I send GET request for crypto <symbol>
  Then price change should be less than <maxPriceChange>
  Examples:
  | symbol    | maxPriceChange |
  | "LTCUSDT" |       75       |
  | "ETHUSDT" |       200      |
  | "BNBUSDT" |       55       |
  | "XRPUSDT" |       80       |
```

becomes (the actual pilot, at
[`CryptoApiPriceChangeDataDrivenTestNgTest`](../../src/test/java/com/znsio/teswiz/testng/CryptoApiPriceChangeDataDrivenTestNgTest.java)):

```java
public class CryptoApiPriceChangeDataDrivenTestNgTest {

    @DataProvider(name = "cryptoSymbolsAndMaxPriceChange", parallel = true)
    public Object[][] cryptoSymbolsAndMaxPriceChange() {
        return new Object[][]{
                {"LTCUSDT", 75},
                {"ETHUSDT", 200},
                {"BNBUSDT", 55},
                {"XRPUSDT", 80},
        };
    }

    @Test(dataProvider = "cryptoSymbolsAndMaxPriceChange", groups = {"api", "cryptoAPI", "priceChange"})
    public void validatePriceChangeInLast24Hrs(String symbol, int maxPriceChange) {
        CryptoAPIBL cryptoApi = new CryptoAPIBL();
        Response jsonResponse = cryptoApi.getDataUsingCryptoSymbol(symbol);
        cryptoApi.verifypriceChange(jsonResponse, maxPriceChange);
    }
}
```

Each `Examples` row becomes one row of the `Object[][]`, copied verbatim (same types, same values); the
outline's placeholder-templated steps become one parameterized `@Test` method with matching parameters.

---

## What doesn't migrate cleanly

Be honest with yourself about these before assuming a scenario is a trivial port:

* **Step defs with real branching logic** based on argument values (not just parameter substitution) - the
  call sequence isn't a flat copy-paste in these cases; understand the logic before flattening it.
* **BDD readability/collaboration value.** A `Given`/`When`/`Then` narrative exists for non-engineers to
  read. That's lost once flattened into Java method calls with no equivalent. If it matters for your team,
  consider a comment above each call preserving the original step text.
- **`Scenario` object usage** (Cucumber's own hook parameter, used for logging/attachments/tagging). Use
  `Hooks.beforeScenario(String)` / `Hooks.afterScenario(String, boolean)` - the TestNG-mode overloads that
  do the same setup/teardown without needing a `Scenario` instance. `TeswizTestNgListener` already wires
  these to TestNG's `ITestListener` lifecycle for you; you don't call them yourself in a pilot test.
* **Ambiguous step-text matches** - if two step defs could both match a given scenario's step text, you're
  relying on Cucumber's own runtime matcher to disambiguate; a naive text-based mapping could pick wrong.
  When unsure which step-def a step maps to, run the Cucumber scenario and check the log/report for the
  step's `match.location` (the exact method Cucumber invoked).
* **`DataTable`/`DocString` step arguments** translate to plain Java collections/strings, but need
  per-case translation - there's no one-size-fits-all mapping.

---

## Reporting parity

Once migrated, TestNG mode gives you the same reporting fidelity as Cucumber mode:

* Hard-gate logic (`SET_HARD_GATE`/`IS_FAILING_TEST_SUITE`) works identically in both modes.
* A rich, masterthought-powered HTML report (Bootstrap navbar, Chart.js tag charts, per-scenario step
  trees with real execution order and nesting depth) is generated automatically, alongside a lightweight
  tag-coverage report and TestNG's own `EmailableReporter2` output. See
  [TestNG-Execution-Mode-Plan.md](TestNG-Execution-Mode-Plan.md) for the implementation details.
* ReportPortal integration for TestNG mode is not available - see
  [TestNG-Execution-Mode-Plan.md](TestNG-Execution-Mode-Plan.md)'s Phase 3 for the investigation and why.

---

## Reference examples in this repo

Every TestNG pilot already committed under
[`src/test/java/com/znsio/teswiz/testng/`](../../src/test/java/com/znsio/teswiz/testng/) is a real,
verified migration of an existing Cucumber scenario, calling the exact same business-layer classes.
Worth reading before migrating your own:

| Pilot | Migrated from | Demonstrates |
|---|---|---|
| `InteractiveCalculatorCliTestNgTest` | `cli.feature` | Simplest single-scenario port, no driver setup |
| `CryptoApiPriceChangeDataDrivenTestNgTest` | `cyptoAPI.feature` (Scenario Outline) | `@DataProvider` mapping |
| `GoogleSearchWebTestNgTest` | `googlesearch.feature` | Driver creation + BL call, Selenium/Playwright-Java/-TS |
| `GoogleSearchAndroidTestNgTest` | `googlesearch.feature` (Android tags) | Android driver setup |
| `MultiUserWebSearchTestNgTest` | `multiuser-multidevice.feature` | Multi-persona orchestration |
| `PDFValidationTestNgTest` | `pdf.feature` | Applitools-backed validation, no Gherkin table |
| `TheAppInvalidLoginTestNgTest` | `theapp.feature` | Setup/test-flow separation, shown above |
| `JioMeetMicSettingsTestNgTest` | `jiomeet.feature` | Fluent BL chaining (`AuthBL` → `LandingBL` → `InAMeetingBL`) |
