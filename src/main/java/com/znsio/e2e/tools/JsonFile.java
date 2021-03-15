package com.znsio.e2e.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.exceptions.InvalidTestDataException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class JsonFile {
    private JsonFile() {}

    public static void saveJsonToFile (Map<String, Map> jsonMap, String fileName) {
        System.out.printf("Save the following json to file: '%s'%n'%s'%n", fileName, jsonMap);
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsonMap, writer);
        } catch (IOException e) {
            throw new EnvironmentSetupException(String.format("Unable to save following json to file: '%s'%n'%s'%n", jsonMap, fileName), e);
        }
    }

    public static Map<String, Map> getNodeValueAsMapFromJsonFile (String node, String fileName) {
        Map<String, Map> map = loadJsonFile(fileName);
        System.out.printf("Platform: '%s'%n", node);
        Map<String, Map> envMap = map.get(node);
        System.out.println("Loaded map: " + envMap);
        if (null == envMap) {
            throw new InvalidTestDataException(String.format("Node: '%s' not found in file: '%s'",node, fileName ));
        }
        return envMap;
    }

    public static Map<String, Map> loadJsonFile (String fileName) {
        System.out.printf("Loading Json file: '%s'%n", fileName);
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

    public static String getNodeValueAsStringFromJsonFile (String fileName, String[] nodeTree) {
        Map<String, Map> map = loadJsonFile(fileName);

        String nodePath = "";
        for (int nodeCount = 0; nodeCount < nodeTree.length - 1; nodeCount++) {
            System.out.printf("Finding node: '%s'%n", nodeTree[nodeCount]);
            nodePath += nodeTree[nodeCount] + " -> ";
            map = map.get(nodeTree[nodeCount]);
            if (null == map) {
                throw new InvalidTestDataException(String.format("Node: '%s' not found in file: '%s'",nodePath, fileName ));
            }
        }
        String retValue = String.valueOf(map.get(nodeTree[nodeTree.length - 1]));
        System.out.println("Found value: " + retValue);
        return retValue;
    }

    public static ArrayList<Map> getNodeValueAsArrayListFromJsonFile (String node, String fileName) {
        Map<String, Map> map = loadJsonFile(fileName);
        System.out.printf("Platform: '%s'%n", node);
        ArrayList<Map> envMap = (ArrayList<Map>) map.get(node);
        System.out.println("Loaded arraylist: " + envMap);
        return envMap;
    }
}
