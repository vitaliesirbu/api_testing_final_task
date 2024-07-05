package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class UserDeleteTest {

    private ZipCodeClient zipCodeClient;
    private UserClient userClient;
    private User testUser;
    private List<String> initialZipCodes;

    @Before
    public void setUp() throws Exception {
        zipCodeClient = new ZipCodeClient();
        userClient = new UserClient();

        initialZipCodes = zipCodeClient.getZipCodes();
        String zipCode = initialZipCodes.get(0);

        testUser = UserDataGenerator.generateUniqueUserDataWithZipCode(zipCode);
        CloseableHttpResponse response = userClient.createUser(testUser);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    }

    @After
    public void tearDown() throws Exception {
        zipCodeClient.close();
        userClient.close();
    }

    @Test
    @SneakyThrows
    public void testDeleteUserSuccessfully() {

        CloseableHttpResponse response = userClient.deleteUser(testUser);

        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        Assert.assertFalse(users.stream().anyMatch(u -> u.getName().equals(testUser.getName())));

        List<String> availableZipCodes = zipCodeClient.getZipCodes();
        Assert.assertTrue(availableZipCodes.contains(testUser.getZipCode()));
    }

    @Test
    @SneakyThrows
    public void testDeleteUserWithRequiredFieldsOnly() {

        User userToDelete = new User(testUser.getName(), testUser.getSex(), 0, "");
        CloseableHttpResponse response = userClient.deleteUser(userToDelete);

        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        Assert.assertFalse(users.stream().anyMatch(u -> u.getName().equals(testUser.getName())));

        List<String> availableZipCodes = zipCodeClient.getZipCodes();
        Assert.assertTrue(availableZipCodes.contains(testUser.getZipCode()));
    }

    @Test
    @SneakyThrows
    public void testDeleteUserWithMissingRequiredField() {

        User incompleteUser = new User(testUser.getName(), null, 0, "");

        CloseableHttpResponse response = userClient.deleteUser(incompleteUser);

        Assert.assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        Assert.assertTrue(users.stream().anyMatch(u -> u.getName().equals(testUser.getName())));
    }
}