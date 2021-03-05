package com.znsio.e2e.context;

import com.applitools.eyes.BatchInfo;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.google.gson.Gson;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.Visual;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Session extends SessionContext {

    public static final Platform platform = Platform.valueOf(System.getenv("Platform"));
    public static final String jioMeetEnvironment = System.getenv("targetEnvironment");
    public static final Map<String, String> environmentConfiguration = loadEnvironmentConfiguration(jioMeetEnvironment);
    public static final Map<String, String> testDataForEnvironment = loadTestDataForEnvironment(jioMeetEnvironment);
    private static final String launchName = System.getenv("rp.launch");
    private static final String TESTDATA = "./src/test/resources/testData.json";
    private static final String ENVIRONMENT_CONFIG = "./src/test/resources/environments.json";

    private static Map<String, String> loadTestDataForEnvironment (String environment) {
        return loadJsonInMap(environment, TESTDATA);
    }

    private static final List<String> envs = Arrays.asList("replica", "stage1", "stage2");
    public static final BatchInfo batchName = new BatchInfo(launchName + "-" + jioMeetEnvironment);
    public static final boolean isVisualTestingEnabled = Boolean.parseBoolean(System.getenv("Visual"));

    public static Driver fetchDriver (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona);
    }

    public static Visual fetchEyes (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona).getVisual();
    }

    private static Map<String, String> loadEnvironmentConfiguration (String environment) {
        return loadJsonInMap(environment, ENVIRONMENT_CONFIG);
    }

    private static Map<String, String> loadJsonInMap (String environment, String fileName) {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(fileName));
            Map<String, Map> map = gson.fromJson(reader, Map.class);
            Map<String, String> envMap = map.get(environment);
            System.out.println("envMap: " + envMap);
            reader.close();
            return envMap;
        } catch (IOException e) {
            throw new InvalidTestDataException(String.format("Invalid environment: '%s' provided", environment), e);
        }
    }

    public static SoftAssertions getSoftAssertion (long threadId) {
        return (SoftAssertions) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.SOFT_ASSERTIONS);
    }

    public static Driver setCurrentDriverForUser (String userPersona, Platform forPlatform, TestExecutionContext context) {
        Drivers allDrivers = (Drivers) context.getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.setDriverFor(userPersona, forPlatform, context);
    }

    public static Platform fetchPlatform (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getPlatformForUser(userPersona);
    }

    public static void closeAllDrivers (long threadId) {
        TestExecutionContext context = getTestExecutionContext(threadId);
        Drivers allDrivers = (Drivers) context.getTestState(TEST_CONTEXT.ALL_DRIVERS);
        allDrivers.attachLogsAndCloseAllWebDrivers(context);
    }
}
