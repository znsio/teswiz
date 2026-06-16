# Debugging a test in teswiz

This guide collects the most useful places to look when a test fails or behaves unexpectedly.

## 1. Start with the run configuration

Check the values that control where and how the test runs:

- `PLATFORM`
- `TAG`
- `PARALLEL`
- `IS_VISUAL`
- `SET_HARD_GATE`
- `IS_FAILING_TEST_SUITE`
- `LOG_DIR`
- `LOG_PROPERTIES_FILE`
- `SHOW_SENSITIVE_DATA`

The full list of supported configuration keys is in [ConfigurationParameters-README.md](ConfigurationParameters-README.md).

## 2. Re-run the smallest useful slice

When you are debugging a failure, rerun only the scenario or tag you care about.

Examples:

```bash
PLATFORM=android TAG=@schedule ./gradlew run
```

```bash
PLATFORM=android TAG="@schedule and @signup" ./gradlew run
```

```bash
PLATFORM=web TAG="@schedule or @signup" ./gradlew run
```

The sample commands above come from [ConfiguringTestExecution-README.md](ConfiguringTestExecution-README.md).

## 3. Attach a debugger

If you need to pause execution and step through the test, run Gradle in debug mode:

```bash
./gradlew run -Ddebug=true
```

The `run` task is configured to start the JVM with JDWP on port `5005` when `-Ddebug=true` is set.

## 4. Turn on the right logs

teswiz already supports several log sources that are useful during investigation:

- Appium logs
- browser logs
- device logs
- ReportPortal logs
- AspectJ auto-logging
- Applitools logs when visual testing is enabled

Helpful references:

- [ReportPortal-README.md](ReportPortal-README.md)
- [AspectJLogging-README.md](AspectJLogging-README.md)
- [RunningVisualTests-README.md](RunningVisualTests-README.md)

## 5. Check failure artifacts

When a test fails, look for:

- screenshots or visual diffs if `IS_VISUAL=true`
- ReportPortal attachments
- browser or device logs
- console output produced with your configured log level

For visual testing, the most relevant setup and naming rules are documented in [RunningVisualTests-README.md](RunningVisualTests-README.md).

## 6. Use the failure mode intentionally

If you are working on a known failing scenario, use the hard gate controls to run just the failing suite:

```bash
SET_HARD_GATE=true IS_FAILING_TEST_SUITE=true ./gradlew run
```

If you are validating the pass-path, run with failing tests excluded:

```bash
SET_HARD_GATE=true IS_FAILING_TEST_SUITE=false ./gradlew run
```

See [HardGate.md](HardGate.md) for details.

## 7. If the problem is in test wiring

If the test fails before the app flow even starts, check:

- the config file used for the run
- capability values under `caps/`
- browser config values under `configs/`
- any overridden `BASE_URL_FOR_WEB` or `BROWSER_CONFIG_FILE`

The override pattern is documented in [ConfigurationParameters-README.md](ConfigurationParameters-README.md).

## 8. If you need a starting checklist

1. Confirm the platform and tag are correct.
2. Rerun the single failing scenario.
3. Enable the most relevant logs.
4. Check the output directory and attached artifacts.
5. Narrow the issue to either test setup, app behavior, or assertion logic.
