package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCreationTest {

    private ZipCodeClient zipCodeClient;
    private  UserClient userClient;

    @Before
    public void setUp(){
        zipCodeClient = new ZipCodeClient();
        userClient = new UserClient();
    }

    @After
    public void tearDown() throws Exception{
        zipCodeClient.close();
        userClient.close();
    }

    @Test
    public void testCreateUserWithAllFields() throws Exception{

        List<String> initialZipCodes = zipCodeClient.getZipCodes();
        String zipCodeToUse = initialZipCodes.get(0);

        Map<String, Object> userData = UserDataGenerator.generateUniqueUserData();
        userData.put("zipCode", zipCodeToUse);

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("name").equals(userData.get("name")));
        Assert.assertTrue("User was not added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertFalse("Zip code was not removed from available zip codes", updatedZipCodes.contains(zipCodeToUse));
    }

    @Test
    public void testCreateUserWithRequiredFields() {

        Map<String, Object> userData = UserDataGenerator.generateRequiredUserData();

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("name").equals(userData.get("name")) &&
                        user.get("sex").equals(userData.get("sex")));
        Assert.assertTrue("User was not added to the application", userFound);
    }

    @Test
    public void testCreateUserWithIncorrectZipCode() throws Exception {

        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        String unavailableZipCode = ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes);

        Map<String, Object> userData = UserDataGenerator.generateUniqueUserData();
        userData.put("zipCode", unavailableZipCode);

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals("Expected 424 Failed Dependency for incorrect zip code",
                HttpStatus.SC_FAILED_DEPENDENCY,
                response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("name").equals(userData.get("name")));
        Assert.assertFalse("User should not have been added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertEquals("Available zip codes should remain unchanged",
                availableZipCodes, updatedZipCodes);
    }

    @Test
    public void testCreateUserWithExistingNameAndSex() throws Exception {
        // Generate initial user data
        Map<String, Object> initialUserData = UserDataGenerator.generateUniqueUserData();
        initialUserData.put("zipCode", zipCodeClient.getZipCodes().get(0));

        CloseableHttpResponse initialResponse = userClient.createUser(initialUserData);
        Assert.assertEquals(HttpStatus.SC_CREATED, initialResponse.getStatusLine().getStatusCode());

        // Create duplicate user data with the same name and sex, but different age and zip code
        Map<String, Object> duplicateUserData = new HashMap<>(initialUserData);
        duplicateUserData.put("age", (int)initialUserData.get("age") + 5);  // Different age
        duplicateUserData.put("zipCode", zipCodeClient.getZipCodes().get(1));  // Different zip code

        CloseableHttpResponse duplicateResponse = userClient.createUser(duplicateUserData);

        Assert.assertEquals("Expected 400 Bad Request for duplicate name and sex",
                HttpStatus.SC_BAD_REQUEST,
                duplicateResponse.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        long count = users.stream()
                .filter(user -> user.get("name").equals(initialUserData.get("name")) &&
                        user.get("sex").equals(initialUserData.get("sex")))
                .count();
        Assert.assertEquals("Only one user with the same name and sex should exist", 1, count);

        boolean duplicateUserFound = users.stream()
                .anyMatch(user -> user.get("name").equals(initialUserData.get("name")) &&
                        user.get("age").equals(duplicateUserData.get("age")));
        Assert.assertFalse("Duplicate user should not have been added to the application", duplicateUserFound);
    }
}
