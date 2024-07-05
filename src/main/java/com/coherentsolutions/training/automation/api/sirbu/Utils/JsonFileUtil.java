package com.coherentsolutions.training.automation.api.sirbu.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class JsonFileUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> File createJsonFile(List<T> objects, String prefix) throws Exception {
        File tempFile = Files.createTempFile(prefix, ".json").toFile();
        objectMapper.writeValue(tempFile, objects);
        return tempFile;
    }
}