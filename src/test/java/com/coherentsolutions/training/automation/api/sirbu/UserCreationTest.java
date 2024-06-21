package com.coherentsolutions.training.automation.api.sirbu;

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

        Map<String, Object> userData = new HashMap<>();
        userData.put("age", "30");
        userData.put("name", "Nikola Tesla");
        userData.put("sex", "MALE");
        userData.put("zipCode", zipCodeToUse);

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("name").equals("Nikola Tesla"));
        Assert.assertTrue("User was not added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertFalse("Zip code was not removed from available zip codes", updatedZipCodes.contains(zipCodeToUse));

    }

    @Test
    public void testCreateUserWithRequiredFields() {

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "Nostradamus");
        userData.put("sex", "MALE");

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("sex").equals("MALE"));
        Assert.assertTrue("User was not added to the application", userFound);
    }

    @Test
    public void testCreateUserWithIncorrectZipCode() throws Exception {

        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        // Generate an unavailable zip code
        String unavailableZipCode = ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "Alice");
        userData.put("sex", "FEMALE");
        userData.put("age", 28);
        userData.put("zipCode", unavailableZipCode);

        CloseableHttpResponse response = userClient.createUser(userData);

        Assert.assertEquals("Expected 424 Failed Dependency for incorrect zip code",
                HttpStatus.SC_FAILED_DEPENDENCY,
                response.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(user -> user.get("name").equals("Alice"));
        Assert.assertFalse("User should not have been added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertEquals("Available zip codes should remain unchanged",
                availableZipCodes, updatedZipCodes);
    }

    @Test
    public void testCreateUserWithExistingNameAndSex() throws Exception {
        Map<String, Object> initialUserData = new HashMap<>();
        initialUserData.put("name", "Platon");
        initialUserData.put("sex", "MALE");
        initialUserData.put("age", 30);
        initialUserData.put("zipCode", zipCodeClient.getZipCodes().get(0));

        CloseableHttpResponse initialResponse = userClient.createUser(initialUserData);
        Assert.assertEquals(HttpStatus.SC_CREATED, initialResponse.getStatusLine().getStatusCode());

        Map<String, Object> duplicateUserData = new HashMap<>();
        duplicateUserData.put("name", "Platon");
        duplicateUserData.put("sex", "MALE");
        duplicateUserData.put("age", 35);  // Different age
        duplicateUserData.put("zipCode", zipCodeClient.getZipCodes().get(1));  // Different zip code

        CloseableHttpResponse duplicateResponse = userClient.createUser(duplicateUserData);

        Assert.assertEquals("Expected 400 Bad Request for duplicate name and sex",
                HttpStatus.SC_BAD_REQUEST,
                duplicateResponse.getStatusLine().getStatusCode());

        List<Map<String, Object>> users = userClient.getUsers();
        long count = users.stream()
                .filter(user -> user.get("name").equals("Platon") && user.get("sex").equals("MALE"))
                .count();
        Assert.assertEquals("Only one user with the same name and sex should exist", 1, count);

        boolean duplicateUserFound = users.stream()
                .anyMatch(user -> user.get("name").equals("Platon"));
        Assert.assertFalse("Duplicate user should not have been added to the application", duplicateUserFound);
    }
}
