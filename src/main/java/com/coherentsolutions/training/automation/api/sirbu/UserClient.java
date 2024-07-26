package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Data.UserUpdateDTO;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Attachment;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.SneakyThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserClient {

    private final ObjectMapper objectMapper;
    private final AuthProvider authProvider;
    private final String usersUrl;
    private final String usersUploadUrl;

    public UserClient() {
        this.objectMapper = new ObjectMapper();
        this.authProvider = AuthProvider.getInstance();
        this.usersUrl = ConfigLoader.getProperty("user.url");
        this.usersUploadUrl = ConfigLoader.getProperty("users.url");
    }

    @SneakyThrows
    public Response createUser(User user) {
        String token = authProvider.getWriteToken();

        addPayloadToReport("User creation payload", user);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(objectMapper.writeValueAsString(user))
                .post(usersUrl);
    }

    @SneakyThrows
    public Response uploadUsers(File jsonFile) {
        String token = authProvider.getWriteToken();

        return RestAssured.given()
                .auth().oauth2(token)
                .multiPart("file", jsonFile, ContentType.JSON.toString())
                .post(usersUploadUrl);
    }

    public List<User> generateValidUsers(int count, List<String> availableZipCodes) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count && i < availableZipCodes.size(); i++) {
            users.add(UserDataGenerator.generateUniqueUserDataWithZipCode(availableZipCodes.get(i)));
        }
        return users;
    }

    public User generateUserWithIncorrectZipCode() {
        User user = UserDataGenerator.generateUniqueUserData();
        user.setZipCode("99999");
        return user;
    }

    public User generateUserWIthoutRequiredField() {
        return UserDataGenerator.generateUniqueUserDataWithOutRequiredFields();
    }

    @SneakyThrows
    public Response getUsers(Integer olderThan, String sex, Integer youngerThan) {
        if (olderThan != null && youngerThan != null) {
            return RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("Parameters youngerThan and olderThan can't be specified together")
                    .when().post(usersUrl)
                    .then().statusCode(409)
                    .extract().response();
        }

        String token = authProvider.getReadToken();

        return RestAssured.given()
                .auth().oauth2(token)
                .queryParam("olderThan", olderThan)
                .queryParam("sex", sex)
                .queryParam("youngerThan", youngerThan)
                .get(usersUrl);
    }

    @SneakyThrows
    public List<User> getUsersList(Integer olderThan, String sex, Integer youngerThan) {
        Response response = getUsers(olderThan, sex, youngerThan);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();

        if (statusCode == 409) {
            throw new IllegalArgumentException(responseBody);
        } else if (statusCode != 200) {
            throw new RuntimeException("Unexpected status code: " + statusCode + ". Response: " + responseBody);
        }

        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
    }

    public List<User> getUsers() {
        return getUsersList(null, null, null);
    }
    public void createUsers(List<User> users) {
        for (User user : users) {
            createUser(user);
        }
    }

    @SneakyThrows
    public Response updateUser(UserUpdateDTO updateDTO) {
        String token = authProvider.getWriteToken();

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(objectMapper.writeValueAsString(updateDTO))
                .put(usersUrl);
    }

    @SneakyThrows
    public User getUserByName(String name) {
        List<User> users = getUsers();
        return users.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + name));
    }

    @SneakyThrows
    public Response deleteUser(User user) {
        String token = authProvider.getWriteToken();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("age", user.getAge());
        requestBody.put("name", user.getName());
        requestBody.put("sex", user.getSex());
        requestBody.put("zipCode", user.getZipCode());

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(objectMapper.writeValueAsString(requestBody))
                .delete(usersUrl);
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}
