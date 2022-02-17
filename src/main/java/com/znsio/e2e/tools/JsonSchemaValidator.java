package com.znsio.e2e.tools;

import com.znsio.e2e.exceptions.InvalidTestDataException;
import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonSchemaValidator {
    private static final Logger LOGGER = Logger.getLogger(JsonSchemaValidator.class.getName());

    private static Reader loadJsonFileAsReader(String filePath) {
        try {
            return Files.newBufferedReader(Paths.get(filePath));
        } catch (IOException ioException) {
            throw new InvalidTestDataException(String.format("Unable to load json file: '%s'", filePath), ioException);
        }
    }

    private static InputStream loadJsonResourceFileAsStream(String fileName) {
        String fileAsResource = "/" + fileName;
        return JsonSchemaValidator.class.getResourceAsStream(fileAsResource);
    }

    public static void validateJsonFileAgainstSchema(String jsonFilePath, String schemaFile) {
        try {
            InputStream schemaStream = loadJsonResourceFileAsStream(schemaFile);
            JSONObject schemaStreamObject = new JSONObject(new JSONTokener(schemaStream));
            LOGGER.info("Loaded schema file into a json object: " + schemaFile);

            Reader jsonReader = loadJsonFileAsReader(jsonFilePath);
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonReader));
            LOGGER.info("Loaded json file to be validated against the schema into a json object: " + jsonFilePath);

            Schema schema = SchemaLoader.load(schemaStreamObject);
            schema.validate(jsonObject);
            LOGGER.info(String.format("Json file '%s' validated successfully against the schema", jsonFilePath));
        } catch (ValidationException validationException) {
            throw new InvalidTestDataException(
                    String.format("Json file '%s' failed schema checks:%n%s", jsonFilePath, validationException.getAllMessages()));
        }
    }
}
