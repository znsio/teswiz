package com.znsio.e2e.tools;

import com.google.gson.*;
import com.znsio.e2e.exceptions.*;
import org.apache.log4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JsonFile {
    private static final Logger LOGGER = Logger.getLogger(JsonFile.class.getName());

    private JsonFile() {
    }

    public static void saveJsonToFile(Map<String, Map> jsonMap, String fileName) {
        LOGGER.info("\tSave the following json to file: " + fileName + "   with jsonmap:  " + jsonMap);
        File file = new File(fileName);
        if (file.exists()) {
            LOGGER.info("File: " + file + "  exixts.  Delete it first");
            boolean isFileDeleted = file.delete();
            LOGGER.info("File deleted? " + isFileDeleted);
            if (!isFileDeleted) {
                throw new EnvironmentSetupException("Unable to delete older, already existing capabilities file: " + fileName);
            }
        } else {
            LOGGER.info("File " + file + " does not exist. Create it%n");
        }
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsonMap, writer);
        } catch (IOException e) {
            throw new EnvironmentSetupException(String.format("Unable to save following json to file: '%s'%n'%s'%n", jsonMap, fileName), e);
        }
    }

    public static Map<String, Map> getNodeValueAsMapFromJsonFile(String node, String fileName) {
        Map<String, Map> map = loadJsonFile(fileName);
        LOGGER.info("\tNode: " + node);
        Map<String, Map> envMap = map.get(node);
        LOGGER.info("\tLoaded map: " + envMap);
        if (null == envMap) {
            throw new InvalidTestDataException(String.format("Node: '%s' not found in file: '%s'", node, fileName));
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
            throw new InvalidTestDataException(String.format("Unable to load json file: '%s'", fileName), e);
        }
    }

    public static String getNodeValueAsStringFromJsonFile(String fileName, String[] nodeTree) {
        Map<String, Map> map = loadJsonFile(fileName);

        StringBuilder nodePath = new StringBuilder();
        for (int nodeCount = 0; nodeCount < nodeTree.length - 1; nodeCount++) {
            LOGGER.info("\tFinding node: " + nodeTree[nodeCount]);
            nodePath.append(nodeTree[nodeCount]).append(" -> ");
            map = map.get(nodeTree[nodeCount]);
            if (null == map) {
                throw new InvalidTestDataException(String.format("Node: '%s' not found in file: '%s'", nodePath, fileName));
            }
        }
        String retValue = String.valueOf(map.get(nodeTree[nodeTree.length - 1]));
        LOGGER.info("\tFound value: " + retValue);
        return retValue;
    }

    public static ArrayList<Map> getNodeValueAsArrayListFromJsonFile(String fileName, String node) {
        Map<String, Map> map = loadJsonFile(fileName);
        LOGGER.info("\tPlatform: " + node);
        ArrayList<Map> envMap = (ArrayList<Map>) map.get(node);
        LOGGER.info("\tLoaded arraylist: " + envMap);
        return envMap;
    }

    public static JsonObject convertToMap(String jsonAsString) {
        return JsonParser.parseString(jsonAsString).getAsJsonObject();
    }

    public static JsonArray convertToArray(String jsonAsString) {
        return JsonParser.parseString(jsonAsString).getAsJsonArray();
    }
}
