package com.znsio.teswiz.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.gson.*;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class JsonFile {
    private static final Logger LOGGER = LogManager.getLogger(JsonFile.class.getName());

    private JsonFile() {
    }

    public static void saveJsonToFile(Map<String, Map> jsonMap, String fileName) {
        LOGGER.info("\tSave the following json to file: " + fileName + "   with jsonmap:  " + JsonPrettyPrinter.prettyPrint(jsonMap));
        File file = new File(fileName);
        if (file.exists()) {
            LOGGER.debug("File: " + file + "  exixts.  Delete it first");
            boolean isFileDeleted = file.delete();
            LOGGER.debug("File deleted? " + isFileDeleted);
            if (!isFileDeleted) {
                throw new EnvironmentSetupException(
                        "Unable to delete older, already existing capabilities file: " + fileName);
            }
        } else {
            LOGGER.info("File " + file + " does not exist. Create it\n");
        }
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsonMap, writer);
        } catch (IOException e) {
            throw new EnvironmentSetupException(
                    String.format("Unable to save following json to file: '%s'%n'%s'%n", jsonMap,
                                  fileName), e);
        }
    }

    public static Map<String, Map> getNodeValueAsMapFromJsonFile(String node, String fileName) {
        Map<String, Map> map = loadJsonFile(fileName);
        LOGGER.debug("\tNode: " + node);
        Map<String, Map> envMap = map.get(node);
        LOGGER.debug("\tLoaded map: " + envMap);
        if (null == envMap) {
            throw new InvalidTestDataException(
                    String.format("Node: '%s' not found in file: '%s'", node, fileName));
        }
        return envMap;
    }

    public static Map<String, Map> loadJsonFile(String fileName) {
        LOGGER.info("\tLoading Json file: " + fileName);
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(fileName));
            Map<String, Map> map = gson.fromJson(reader, Map.class);
            reader.close();
            return map;
        } catch (IOException e) {
            throw new InvalidTestDataException(
                    String.format("Unable to load json file: '%s'", fileName), e);
        }
    }

    public static String getNodeValueAsStringFromJsonFile(String fileName, String[] nodeTree) {
        Map<String, Map> map = loadJsonFile(fileName);
        return getValueFromLoadedJsonMap(fileName, nodeTree, map);
    }

    public static String getValueFromLoadedJsonMap(String fileName, String[] nodeTree, Map<String, Map> loadedMap) {
        StringBuilder nodePath = new StringBuilder();
        for (int nodeCount = 0; nodeCount < nodeTree.length - 1; nodeCount++) {
            LOGGER.debug("\tFinding node: " + nodeTree[nodeCount]);
            nodePath.append(nodeTree[nodeCount]).append(" -> ");
            loadedMap = loadedMap.get(nodeTree[nodeCount]);
            if (null == loadedMap) {
                throw new InvalidTestDataException(
                        String.format("Node: '%s' not found in file: '%s'", nodePath, fileName));
            }
        }
        String retValue = String.valueOf(loadedMap.get(nodeTree[nodeTree.length - 1]));
        LOGGER.debug("\tFound value: " + retValue);
        return retValue;
    }

    public static ArrayList<Map> getNodeValueAsArrayListFromJsonFile(String fileName, String node) {
        Map<String, Map> map = loadJsonFile(fileName);
        LOGGER.debug("\tPlatform: " + node);
        ArrayList<Map> envMap = (ArrayList<Map>) map.get(node);
        LOGGER.debug("\tLoaded arraylist: " + envMap);
        return envMap;
    }

    public static JsonObject convertToMap(String jsonAsString) {
        return JsonParser.parseString(jsonAsString).getAsJsonObject();
    }

    public static JsonArray convertToArray(String jsonAsString) {
        return JsonParser.parseString(jsonAsString).getAsJsonArray();
    }

    public static boolean compareFiles(String file1, String file2) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode json1 = objectMapper.readTree(Files.newBufferedReader(Paths.get(file1)));
            JsonNode json2 = objectMapper.readTree(Files.newBufferedReader(Paths.get(file2)));

            JsonNode diff = JsonDiff.asJson(json1, json2);

            if (diff.isEmpty()) {
                LOGGER.info("The JSON files (file1: '%s' and file2: '%s') are identical.");
                return true;
            } else {
                String differencs = getDifferences(diff, json1);
                LOGGER.info("The JSON files (file1: '%s' and file2: '%s') are different.\n%s".formatted(file1, file2, differencs));
                return false;
            }
        } catch (Exception e) {
            throw new InvalidTestDataException("Invalid file provided", e);
        }
    }

    static @NotNull String getDifferences(JsonNode diff, JsonNode jsonNode1) {
        StringBuilder differences = new StringBuilder();

        for (JsonNode change : diff) {
            String operation = change.get("op").asText();
            String path = change.get("path").asText();

            differences.append("\n\tOperation: ").append(operation).append(", Path: ").append(path);

            if (operation.equals("replace") || operation.equals("add")) {
                appendNewValue(differences, change);
            }
            if (operation.equals("replace") || operation.equals("remove")) {
                appendOldValue(differences, path, jsonNode1);
            }
        }

        differences.append("\n");
        return differences.toString();
    }

    private static void appendNewValue(StringBuilder differences, JsonNode change) {
        differences.append("\n\t\tNew Value: ").append(change.get("value"));
    }

    private static void appendOldValue(StringBuilder differences, String path, JsonNode jsonNode1) {
        String[] keys = path.split("/");
        JsonNode parentNode = jsonNode1;

        for (int i = 1; i < keys.length; i++) {
            String key = keys[i];
            if (parentNode == null) {
                differences.append("\n\t\tMissing key or structure for path: ").append(path).append(" in the original file.");
                return;
            }
            parentNode = key.matches("\\d+") ? parentNode.get(Integer.parseInt(key)) : parentNode.get(key);
        }

        if (parentNode != null) {
            differences.append("\n\t\tOld Value: ").append(parentNode);
        } else {
            differences.append("\n\t\tOld Value: null (key does not exist in the original file)");
        }
    }

}
