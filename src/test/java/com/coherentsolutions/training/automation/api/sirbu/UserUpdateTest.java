package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Data.UserUpdateDTO;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class UserUpdateTest {

    private ZipCodeClient zipCodeClient;
    private UserClient userClient;
    private User initialUser;
    private List<String> availableZipCodes;

    @Before
    public void setUp() throws Exception {
        zipCodeClient = new ZipCodeClient();
        userClient = new UserClient();

        availableZipCodes = zipCodeClient.getZipCodes();

        if (availableZipCodes.size() < 2) {
            String newZipCode = zipCodeClient.generateNewZipCode(availableZipCodes.get(0));
            CloseableHttpResponse response = zipCodeClient.postZipCodes(List.of(newZipCode));
            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            availableZipCodes.add(newZipCode);
        }

        initialUser = UserDataGenerator.generateUniqueUserDataWithZipCode(availableZipCodes.get(0));
        CloseableHttpResponse response = userClient.createUser(initialUser);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    }

    @After
    public void tearDown() throws Exception {
        zipCodeClient.close();
        userClient.close();
    }

    @Test
    @SneakyThrows
    public void testUpdateUserSuccessfully() {
        String newName = initialUser.getName() + "_updated";
        String newSex = initialUser.getSex().equals("MALE") ? "FEMALE" : "MALE";
        int newAge = initialUser.getAge() + 1;

        String newZipCode = availableZipCodes.stream()
                .filter(zipCode -> !zipCode.equals(initialUser.getZipCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No alternative zip code available"));

        User newValues = new User(newName, newSex, newAge, newZipCode);

        UserUpdateDTO updateDTO = new UserUpdateDTO(initialUser, newValues);

        CloseableHttpResponse response = userClient.updateUser(updateDTO);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        User retrievedUser = userClient.getUserByName(newName);

        Assert.assertEquals(newName, retrievedUser.getName());
        Assert.assertEquals(newSex, retrievedUser.getSex());
        Assert.assertEquals(newAge, retrievedUser.getAge());
        Assert.assertEquals(newZipCode, retrievedUser.getZipCode());
    }

    @Test
    @SneakyThrows
    public void testUpdateUserWithUnavailableZipCode() {

        String unavailableZipCode = ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes);

        String newName = initialUser.getName() + "_updated";
        String newSex = initialUser.getSex().equals("MALE") ? "FEMALE" : "MALE";
        int newAge = initialUser.getAge() + 1;

        User newValues = new User();
        newValues.setName(newName);
        newValues.setSex(newSex);
        newValues.setAge(newAge);
        newValues.setZipCode(unavailableZipCode);

        UserUpdateDTO updateDTO = new UserUpdateDTO(initialUser, newValues);

        CloseableHttpResponse response = userClient.updateUser(updateDTO);

        Assert.assertEquals(HttpStatus.SC_FAILED_DEPENDENCY, response.getStatusLine().getStatusCode());

        User retrievedUser = userClient.getUserByName(initialUser.getName());
        Assert.assertEquals(initialUser.getName(), retrievedUser.getName());
        Assert.assertEquals(initialUser.getSex(), retrievedUser.getSex());
        Assert.assertEquals(initialUser.getAge(), retrievedUser.getAge());
        Assert.assertEquals(initialUser.getZipCode(), retrievedUser.getZipCode());


        List<String> updatedZipCodes = zipCodeClient.getZipCodes();
        Assert.assertFalse(updatedZipCodes.contains(unavailableZipCode));
    }

    @Test
    public void testUpdateUserOmittingRequiredField() {

        String newName = initialUser.getName();
        String newSex = initialUser.getSex().equals("MALE") ? "FEMALE" : "MALE";
        int newAge = initialUser.getAge();
        String newZipCode = availableZipCodes.stream()
                .filter(zipCode -> !zipCode.equals(initialUser.getZipCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No alternative zip code available"));


        User incompleteNewValues = new User();
        incompleteNewValues.setName(newName);
        incompleteNewValues.setAge(newAge);
        incompleteNewValues.setZipCode(newZipCode);

        UserUpdateDTO updateDTO = new UserUpdateDTO(initialUser, incompleteNewValues);

        CloseableHttpResponse response = userClient.updateUser(updateDTO);

        Assert.assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());

        User retrievedUser = userClient.getUserByName(initialUser.getName());
        Assert.assertEquals(initialUser.getName(), retrievedUser.getName());
        Assert.assertEquals(initialUser.getSex(), retrievedUser.getSex());
        Assert.assertEquals(initialUser.getAge(), retrievedUser.getAge());
        Assert.assertEquals(initialUser.getZipCode(), retrievedUser.getZipCode());
    }
}