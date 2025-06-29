# Run and Collect Tests - GitHub Composite Action

This composite GitHub Action runs test suites with dynamic parameters, captures the exit code, and uploads test artifacts from the `target/` directory.

## 📁 Location

Place this file under:

```
.github/actions/run-and-collect-tests/action.yml
```

## 🧩 Inputs

| Name                    | Required | Description                                                          |
| ----------------------- | -------- | -------------------------------------------------------------------- |
| `id`                    | ✅ Yes    | Short identifier for the test suite. Used in logs and artifact name. |
| `config-path`           | ✅ Yes    | Path to the configuration `.properties` file used for the test.      |
| `tag`                   | ✅ Yes    | Cucumber tag filter (e.g. `@smoke and @web`).                        |
| `platform`              | ✅ Yes    | Platform under test (`web`, `android`, `api`, etc.).                 |
| `applitools_api_key`    | ✅ Yes    | Applitools API key (required for visual tests).                      |
| `browserstack_username` | ✅ Yes    | BrowserStack username (required for cloud tests).                    |
| `browserstack_key`      | ✅ Yes    | BrowserStack access key (required for cloud tests).                  |
| `display-name`          | ✅ Yes    | Human-readable label for the test suite.                             |
| `artifact-name`         | ✅ Yes    | Artifact name to be used for saving test results.                    |
| `is-visual`             | ✅ Yes    | Whether visual testing is enabled (`true` or `false`).               |
| `additional-env`        | ❌ No     | Extra environment variables (space-separated).                       |

## 📤 Outputs

| Name        | Description                    |
| ----------- | ------------------------------ |
| `exit_code` | Exit code from `./gradlew run` |

## 🛠️ Example Usage

```yaml
- name: Run TheApp Android Tests
  id: theapp_android
  uses: ./.github/actions/run-and-collect-tests
  with:
    id: theapp-android
    config-path: ./configs/theapp/theapp_browserstack_config.properties
    tag: "@theapp and @invalidLogin1 and @browserstack"
    platform: android
    applitools_api_key: ${{ secrets.TESWIZ_APPLITOOLS_API_KEY }}
    browserstack_username: ${{ secrets.BROWSERSTACK_CLOUD_USERNAME }}
    browserstack_key: ${{ secrets.BROWSERSTACK_CLOUD_KEY }}
    display-name: "TheApp Android"
    artifact-name: test-results-theapp-android
    is-visual: false
    additional-env: "SET_HARD_GATE=true IS_FAILING_TEST_SUITE=false"
```

## 📦 Artifacts

Artifacts are automatically uploaded from the `target/` directory and saved as:

```
test-results-${{ inputs.id }}
```

If `artifact-name` is provided, that is used instead.

## ✅ Conditional Usage

- Use `exit_code` output to determine success/failure in downstream steps.
- This action supports both local and cloud tests.
- All credentials are required and must be set using secrets.

## 🚀 Benefits

- Standardized test execution across all platforms.
- Automatic exit code handling and artifact upload.
- Simplified workflow maintenance.

---

Need help adapting existing workflows? Ping the maintainer or open an issue.
