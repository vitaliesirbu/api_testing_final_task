package com.coherentsolutions.training.automation.api.sirbu.Utils;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;

import java.util.Random;

public class UserDataGenerator {
    private static final Random random = new Random();

    public static User generateUniqueUserData() {
        return new User(
                generateUniqueName(),
                generateRandomSex(),
                generateRandomAge(),
                null
        );
    }

    public static User generateUniqueUserDataWithZipCode(String zipCode) {
        User user = generateUniqueUserData();
        user.setZipCode(zipCode);
        return user;
    }

    public static User generateRequiredUserData() {
        return new User(
                generateUniqueName(),
                generateRandomSex()
        );
    }

    private static String generateUniqueName() {
        return "User_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }

    private static String generateRandomSex() {
        return random.nextBoolean() ? "MALE" : "FEMALE";
    }

    private static int generateRandomAge() {
        return random.nextInt(100) + 1;
    }
}