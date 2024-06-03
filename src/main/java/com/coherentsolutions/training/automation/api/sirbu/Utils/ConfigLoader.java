package com.coherentsolutions.training.automation.api.sirbu.Utils;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    @SneakyThrows
    public static String getProperty(String key) {
        if (properties.isEmpty()) {
            try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("Unable to find config.properties");
                }
                properties.load(input);
            }
        }
        return properties.getProperty(key);
    }
}
