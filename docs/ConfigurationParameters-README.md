# Configuration parameters

The config.properties file has the following properties. Highlighting the defaults, and options for each here.

These can be overridden by providing the same either as environment variables or system properties.

    # ATD properties
    RUNNER=distribute -> ATD property. We will always use distributed
    FRAMEWORK=cucumber -> ATD property. We will always use cucumber
    RUNNER_LEVEL=methods -> ATD property. We will always use methods
    CAPS=./caps/capabilities.json -> Path to capabilties.json file

    # teswiz configuration properties. Can be overridden using environment variables or system properties

    APP_NAME=teswiz -> Name of your application
    APP_PACKAGE_NAME=io.cloudgrey.the_app -> android app package name
    APP_PATH=./temp/abc.apk -> path to android / windows app name
    APPLITOOLS_CONFIGURATION=./configs/applitools_config.json -> Applitools configuration 
    BASE_URL_FOR_WEB=BASE_URL -> Key name of the property in TEST_DATA_FILE for environment specific base url
    BRANCH_NAME -> Key name of environment variable which should be used to get the current Branch name. 
                   IF this is not specified, then teswiz will try to get the BRANCH_NAME using this command: `git rev-parse --abbrev-ref HEAD`
    BROWSER=chrome -> Which browser to use for Web execution? Supported: chrome || firefox
                      Browsers should to be installed. Corresponding WebDriver for the browser will be downloaded automatically
    BUILD_ID=BUILDID -> The key name of the environment variable that has the corresponding build id of the test execution
    CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION=true -> Uninstall app from local Android devices before starting test execution
    CLOUD_KEY=<auth / api key> for pCloudy / Headspin
    CLOUD_USER=<username / email> for pCloudy -> Not required for Headspin
    CLOUD_NAME=headspin|pCloudy -> REQUIRED when running against pCloudy / Headspin
    CLOUD_UPLOAD_APP=false -> Upload the app to pCloudy / headspin before running the tests
    DEVICE_LAB_URL=<root url for device farm>
    ENVIRONMENT_CONFIG_FILE=./src/test/resources/environments.json -> Environment specific configuration file
    IS_VISUAL=false -> Should enable Applitools Visual Testing? If yes, set to true
    LOG_DIR=target -> Where should logs be created?
    LOG_PROPERTIES_FILE=./src/test/resources/log4j.properties -> log4j configuration file
    MAX_NUMBER_OF_APPIUM_DRIVERS -> The max number of drivers on cloud to create for multiuser android tests, default value is 5
    MAX_NUMBER_OF_WEB_DRIVERS -> The max number of web drivers on cloud to create for multiuser web tests, default value is 5
    PLATFORM=android -> Run tests against? Supported: android | iOS | windows | web
    PARALLEL=1 -> How many tests should be run in parallel?
    PROXY_KEY=HTTP_PROXY -> If proxy should be set, what is the environment variable specifying the proxy?
    PROXY_URL=<proxy_url> -> What is the proxy url to be used if PROXY_KEY is set
    WEBDRIVER_MANAGER_PROXY_KEY=HTTP_PROXY -> If proxy should be used for WebDriverManager, what is the environment variable specifying the proxy?
    WEBDRIVER_MANAGER_PROXY_URL=<proxy_url> -> What is the proxy url to be used for WebDriverManager if WEBDRIVER_MANAGER_PROXY_KEY is set
    REMOTE_WEBDRIVER_GRID_PORT=<environment variable name which holds the port to be used for RemoteWebDriver>
    REPORT_PORTAL_FILE=src/test/resources/reportportal.properties -> ReportPortal.io configuration
    RUN_IN_CI=false -> Are tests running in CI?
    TARGET_ENVIRONMENT=prod -> Which environment are the tests running against? Should map to envrionments specified in ENVIRONMENT_CONFIG_FILE
    TEST_DATA_FILE=./src/test/resources/testData.json -> Environment specific static test data
    BROWSER_CONFIG_FILE=./src/test/resources/com/znsio/e2e/features/configs/browser_config.json -> json containing browser configurations
