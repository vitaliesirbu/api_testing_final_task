package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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

        User user = UserDataGenerator.generateUniqueUserDataWithZipCode(zipCodeToUse);

        CloseableHttpResponse response = userClient.createUser(user);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(u -> u.getName().equals(user.getName()));
        Assert.assertTrue("User was not added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertFalse("Zip code was not removed from available zip codes", updatedZipCodes.contains(zipCodeToUse));
    }

    @Test
    public void testCreateUserWithRequiredFields() {

        User user = UserDataGenerator.generateRequiredUserData();

        CloseableHttpResponse response = userClient.createUser(user);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(u -> u.getName().equals(user.getName()) &&
                        u.getSex().equals(user.getSex()));
        Assert.assertTrue("User was not added to the application", userFound);
    }

    @Test
    public void testCreateUserWithIncorrectZipCode() throws Exception {

        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        String unavailableZipCode = ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes);

        User user = UserDataGenerator.generateUniqueUserDataWithZipCode(unavailableZipCode);

        CloseableHttpResponse response = userClient.createUser(user);

        Assert.assertEquals("Expected 424 Failed Dependency for incorrect zip code",
                HttpStatus.SC_FAILED_DEPENDENCY,
                response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(u -> u.getName().equals(user.getName()));
        Assert.assertFalse("User should not have been added to the application", userFound);

        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertEquals("Available zip codes should remain unchanged",
                availableZipCodes, updatedZipCodes);
    }

    @Test
    public void testCreateUserWithExistingNameAndSex() throws Exception {
        User initialUser = UserDataGenerator.generateUniqueUserDataWithZipCode(zipCodeClient.getZipCodes().get(0));

        CloseableHttpResponse initialResponse = userClient.createUser(initialUser);
        Assert.assertEquals(HttpStatus.SC_CREATED, initialResponse.getStatusLine().getStatusCode());

        User duplicateUser = new User(initialUser.getName(), initialUser.getSex(),
                initialUser.getAge() + 5, zipCodeClient.getZipCodes().get(1));

        CloseableHttpResponse duplicateResponse = userClient.createUser(duplicateUser);

        Assert.assertEquals("Expected 400 Bad Request for duplicate name and sex",
                HttpStatus.SC_BAD_REQUEST,
                duplicateResponse.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        long count = users.stream()
                .filter(u -> u.getName().equals(initialUser.getName()) &&
                        u.getSex().equals(initialUser.getSex()))
                .count();
        Assert.assertEquals("Only one user with the same name and sex should exist", 1, count);

        boolean duplicateUserFound = users.stream()
                .anyMatch(u -> u.getName().equals(initialUser.getName()) &&
                        u.getAge() == duplicateUser.getAge());
        Assert.assertFalse("Duplicate user should not have been added to the application", duplicateUserFound);
        }
    }
