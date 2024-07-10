package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Utils.JsonFileUtil;

import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class UserUploadTest {

    private ZipCodeClient zipCodeClient;
    private UserClient userClient;

    @Before
    public void setUp() {
        zipCodeClient = new ZipCodeClient();
        userClient = new UserClient();
    }

    @After
    public void tearDown() throws Exception {
        zipCodeClient.close();
        userClient.close();
    }

    @Test
    @SneakyThrows
    public void testSuccessfulUserUpload() {
        List<String> availableZipCodes = zipCodeClient.getZipCodes();
        List<User> usersToUpload = userClient.generateValidUsers(3, availableZipCodes);
        File jsonFile = JsonFileUtil.createJsonFile(usersToUpload, "users");

        CloseableHttpResponse response = userClient.uploadUsers(jsonFile);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        String responseBody = EntityUtils.toString(response.getEntity());
        String[] parts = responseBody.split("=");
        Assert.assertEquals("Unexpected response format", 2, parts.length);
        int uploadedCount = Integer.parseInt(parts[1].trim());
        Assert.assertEquals("Number of uploaded users should match", usersToUpload.size(), uploadedCount);

        List<User> updatedUsers = userClient.getUsers();
        Assert.assertEquals("All users should be replaced", usersToUpload.size(), updatedUsers.size());
        for (User user : usersToUpload) {
            Assert.assertTrue("Uploaded user should exist",
                    updatedUsers.stream().anyMatch(u -> u.getName().equals(user.getName()) && u.getSex().equals(user.getSex())));
        }

        jsonFile.delete();
    }
    @Test
    @SneakyThrows
    public void testFailedUserUploadWithIncorrectZipCode() {

        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        List<User> usersToUpload = userClient.generateValidUsers(2, availableZipCodes);

        User userWithIncorrectZipCode = userClient.generateUserWithIncorrectZipCode();
        usersToUpload.add(userWithIncorrectZipCode);

        File jsonFile = JsonFileUtil.createJsonFile(usersToUpload, "users_with_incorrect_zip");

        CloseableHttpResponse response = userClient.uploadUsers(jsonFile);

        Assert.assertEquals(HttpStatus.SC_FAILED_DEPENDENCY, response.getStatusLine().getStatusCode());


        List<User> updatedUsers = userClient.getUsers();
        for (User user : usersToUpload) {
            Assert.assertFalse("User should not be uploaded: " + user.getName(),
                    updatedUsers.stream().anyMatch(u -> u.getName().equals(user.getName())));
        }

        jsonFile.delete();
    }

    @Test
    @SneakyThrows
    public void testFailedUserUploadWithMissingRequiredField() {

        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        List<User> usersToUpload = userClient.generateValidUsers(2, availableZipCodes);

        User userWithMissingField = userClient.generateUserWIthoutRequiredField();
        userWithMissingField.setName("UserWithMissingField");

        usersToUpload.add(userWithMissingField);

        File jsonFile = JsonFileUtil.createJsonFile(usersToUpload, "users_with_missing_field");

        CloseableHttpResponse response = userClient.uploadUsers(jsonFile);

        Assert.assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());

        List<User> updatedUsers = userClient.getUsers();
        for (User user : usersToUpload) {
            Assert.assertFalse("User should not be uploaded: " + user.getName(),
                    updatedUsers.stream().anyMatch(u -> u.getName().equals(user.getName())));
        }

        jsonFile.delete();
    }
}