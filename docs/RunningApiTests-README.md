# API Test Execution:

## Command: 
CONFIG=./configs/<apiConfig.properties> TAG=<ScenarioTag> PLATFORM=api ./gradlew run

## API Config.properties File Params:

```
RUNNER=distribute 
FRAMEWORK=cucumber
RUNNER_LEVEL=methods
APP_NAME=<API SCENARIO APP NAME>
ENVIRONMENT_CONFIG_FILE=./src/test/resources/environments.json
IS_VISUAL=false
LOG_DIR=target
LOG_PROPERTIES_FILE=./src/test/resources/log4j.properties
PARALLEL=<parellel count>
PLATFORM=api
PROXY_KEY=HTTP_PROXY
REPORT_PORTAL_FILE=src/test/resources/reportportal.properties
RUN_IN_CI=<true/false>
TARGET_ENVIRONMENT=<sit/eat/prod>
LAUNCH_NAME_SUFFIX= on <environment> Environment
TEST_DATA_FILE=./src/test/resources/testData.json
```

## Sample API scenario Example:

```
feature file: src/test/resources/com/znsio/teswiz/features/weatherAPI.feature
Step Definition File: src/test/java/com/znsio/teswiz/steps/WeatherAPISteps.java
BL File: src/test/java/com/znsio/teswiz/businessLayer/weatherAPI/WeatherAPIBL.java
```

## Sample API Workflow scenario Example:

```
feature file: src/test/resources/com/znsio/teswiz/features/workflow/weatherAPI.feature
Step Definition File: src/test/java/com/znsio/teswiz/steps/WeatherAPISteps.java
BL File: src/test/java/com/znsio/teswiz/businessLayer/weatherAPI/WeatherAPIBL.java
```

## Reportportal Execution report:

To get reportportal execution report, Update following params in
src/test/resources/reportportal.properties

```
rp.endpoint=http://127.0.0.1:8080
rp.uuid=<uuid from reportportal dashboard>
rp.launch=teswiz
rp.project=teswiz
rp.enable=false
```