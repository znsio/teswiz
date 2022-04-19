package com.znsio.e2e.tools;

import com.epam.reportportal.service.*;
import com.znsio.e2e.exceptions.*;
import org.apache.log4j.*;
import org.everit.json.schema.*;
import org.everit.json.schema.loader.*;
import org.json.*;

import java.io.*;
import java.util.*;

public class JsonSchemaValidator {
    private static final Logger LOGGER = Logger.getLogger(JsonSchemaValidator.class.getName());

    private static InputStream loadJsonResourceFileAsStream(String fileName) {
        String fileAsResource = "/" + fileName;
        return JsonSchemaValidator.class.getResourceAsStream(fileAsResource);
    }

    public static JSONObject validateJsonFileAgainstSchema(String jsonFilePath, String jsonContents, String schemaFile) {
        try {
            InputStream schemaStream = loadJsonResourceFileAsStream(schemaFile);
            JSONObject schemaStreamObject = new JSONObject(new JSONTokener(schemaStream));
            LOGGER.info("Loaded schema file into a json object: " + schemaFile);

            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonContents));
            LOGGER.info(String.format("Loaded json contents from file '%s' to be validated against the schema into a json object", jsonFilePath));

            Schema schema = SchemaLoader.load(schemaStreamObject);
            schema.validate(jsonObject);
            LOGGER.info(String.format("Json file '%s' validated successfully against the schema", jsonFilePath));
            return jsonObject;
        } catch (ValidationException validationException) {
            String exceptionMessage = String.format("Json file '%s' failed schema checks:%n%s", jsonFilePath, validationException.getAllMessages());
            LOGGER.info(exceptionMessage);
            ReportPortal.emitLog(exceptionMessage, "DEBUG", new Date());
            throw new InvalidTestDataException(exceptionMessage);
        }
    }
}
