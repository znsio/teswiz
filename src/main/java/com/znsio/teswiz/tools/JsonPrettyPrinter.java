package com.znsio.teswiz.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

public class JsonPrettyPrinter {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String prettyPrint(Object input) {
        try {
            Object expanded = parseValue(input);
            return mapper.writeValueAsString(expanded);
        } catch (Exception e) {
            return "⚠️ Failed to pretty print JSON: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private static Object parseValue(Object value) {
        if (value instanceof String str) {
            // Try parsing as Map
            try {
                return parseValue(mapper.readValue(str, Map.class));
            } catch (JsonProcessingException ignored) {}
            // Try parsing as List
            try {
                return parseValue(mapper.readValue(str, List.class));
            } catch (JsonProcessingException ignored) {}
            return str; // it's a raw string
        } else if (value instanceof Map<?, ?> map) {
            Map<String, Object> castedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                castedMap.put(String.valueOf(entry.getKey()), parseValue(entry.getValue()));
            }
            return castedMap;
        } else if (value instanceof List<?> list) {
            List<Object> parsedList = new ArrayList<>();
            for (Object item : list) {
                parsedList.add(parseValue(item));
            }
            return parsedList;
        } else {
            return value;
        }
    }
}
