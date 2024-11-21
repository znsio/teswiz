package com.znsio.teswiz.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class YamlFile {
    private static final Logger LOGGER = LogManager.getLogger(YamlFile.class.getName());

    private YamlFile() {
    }

    public static boolean compareFiles(String file1, String file2) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yaml1;
            Map<String, Object> yaml2;

            try (InputStream input1 = Files.newInputStream(Paths.get(file1));
                 InputStream input2 = Files.newInputStream(Paths.get(file2))) {
                yaml1 = yaml.load(input1);
                yaml2 = yaml.load(input2);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode1 = objectMapper.valueToTree(yaml1);
            JsonNode jsonNode2 = objectMapper.valueToTree(yaml2);

            JsonNode diff = JsonDiff.asJson(jsonNode1, jsonNode2);

            if (diff.isEmpty()) {
                LOGGER.info("The YAML files (file1: '%s' and file2: '%s') are identical.");
                return true;
            } else {
                String differencs = JsonFile.getDifferences(diff, jsonNode1);
                LOGGER.info("The YAML files (file1: '%s' and file2: '%s') are different.\n%s".formatted(file1, file2, differencs));
                return false;
            }
        } catch (Exception e) {
            throw new InvalidTestDataException("Invalid file provided", e);
        }
    }
}
