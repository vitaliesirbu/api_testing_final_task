package com.coherentsolutions.training.automation.api.sirbu.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserDataGenerator {
    private static final Random random = new Random();

    public static Map<String, Object> generateUniqueUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", generateUniqueName());
        userData.put("sex", generateRandomSex());
        userData.put("age", generateRandomAge());
        return userData;
    }

    private static String generateUniqueName() {
        return "User_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }

    private static String generateRandomSex() {
        return random.nextBoolean() ? "MALE" : "FEMALE";
    }

    private static int generateRandomAge() {
        return random.nextInt(100) + 1; // Generate age between 1 and 100
    }

    public static Map<String, Object> generateUniqueUserDataWithZipCode(String zipCode) {
        Map<String, Object> userData = generateUniqueUserData();
        userData.put("zipCode", zipCode);
        return userData;
    }
    public static Map<String, Object> generateRequiredUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", generateUniqueName());
        userData.put("sex", generateRandomSex());
        return userData;
    }
}
