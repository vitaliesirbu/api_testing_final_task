package sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.UserClient;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import com.coherentsolutions.training.automation.api.sirbu.ZipCodeClient;
import io.qameta.allure.Attachment;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserCreationTest {

    private ZipCodeClient zipCodeClient;
    private UserClient userClient;

    @Before
    public void setUp() throws IOException {
        zipCodeClient = new ZipCodeClient();
        userClient = new UserClient();

        List<String> availableZipCodes = zipCodeClient.getZipCodes();
        if (availableZipCodes.size() < 2) {
            List<String> newZipCodes = Arrays.asList("12345", "67890");
            zipCodeClient.postZipCodes(newZipCodes);
        }
    }

    @After
    public void tearDown() throws Exception{
        zipCodeClient.close();
        userClient.close();
    }

    @Test
    @SneakyThrows
    @Issue("User Creation")
    @Step("Create a user with all fields")
    public void testCreateUserWithAllFields() {

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

        addPayloadToReport("Updated users list", users);
        addPayloadToReport("Updated zip codes", updatedZipCodes);
    }

    @Test
    @SneakyThrows
    @Issue("User Creation")
    @Step("Create a user with all required fields")
    public void testCreateUserWithRequiredFields() {

        User user = UserDataGenerator.generateRequiredUserData();

        CloseableHttpResponse response = userClient.createUser(user);

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsers();
        boolean userFound = users.stream()
                .anyMatch(u -> u.getName().equals(user.getName()) &&
                        u.getSex().equals(user.getSex()));
        Assert.assertTrue("User was not added to the application", userFound);

        addPayloadToReport("Updated users list", users);
    }

    @Test
    @SneakyThrows
    @Issue("User Creation")
    @Step("Create a user with incorrect Zip Code")
    public void testCreateUserWithIncorrectZipCode() {

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

        addPayloadToReport("Updated users list", users);
        addPayloadToReport("Updated zip codes", updatedZipCodes);
    }

    @Test
    @SneakyThrows
    @Issue("User Creation")
    @Step("Create a duplicate user")
    public void testCreateUserWithExistingNameAndSex() {
        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        if (availableZipCodes.size() < 2) {
            throw new IllegalStateException("Not enough zip codes available for this test. Need at least 2.");
        }

        User initialUser = UserDataGenerator.generateUniqueUserDataWithZipCode(availableZipCodes.get(0));

        addPayloadToReport("Initial user creation payload", initialUser);

        CloseableHttpResponse initialResponse = userClient.createUser(initialUser);
        Assert.assertEquals(HttpStatus.SC_CREATED, initialResponse.getStatusLine().getStatusCode());

        User duplicateUser = new User(initialUser.getName(), initialUser.getSex(),
                initialUser.getAge() + 5, availableZipCodes.get(1));

        addPayloadToReport("Duplicate user creation payload", duplicateUser);

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

        addPayloadToReport("Updated users list", users);
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private void addPayloadToReport(String attachmentName, Object payload) {
        payload.toString();
    }

}
