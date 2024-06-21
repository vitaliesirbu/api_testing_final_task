package com.coherentsolutions.training.automation.api.sirbu.Utils;

import java.util.List;
import java.util.Random;

public class ZipCodeGenerator {

    private static final Random random = new Random();
    public static String generateUnavailableZipCode(List<String> availableZipCodes) {
        String unavailableZipCode;
        do {
            unavailableZipCode = generateRandomZipCode();
        } while (availableZipCodes.contains(unavailableZipCode));
        return unavailableZipCode;
    }
    private static String generateRandomZipCode() {
        return String.format("%05d", random.nextInt(100000));
    }
}