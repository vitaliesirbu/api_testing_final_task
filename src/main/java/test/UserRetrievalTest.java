package test;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.UserClient;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import io.qameta.allure.Attachment;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserRetrievalTest {

    private UserClient userClient;
    private List<User> predefinedUsers;

    @Before
    public void setUp() {
        userClient = new UserClient();
        predefinedUsers = createPredefinedUsers();
        userClient.createUsers(predefinedUsers);
    }

    @After
    public void tearDown() throws Exception {userClient.close();

    }

    private List<User> createPredefinedUsers() {
        return IntStream.range(0, 10)
                .mapToObj(i -> UserDataGenerator.generateUniqueUserData())
                .collect(Collectors.toList());
    }

    @Test
    @Issue("User Retrieval")
    @Step("Get all available users")
    public void testGetAllUsers() {
        CloseableHttpResponse response = userClient.getUsers(null, null, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> retrievedUsers = userClient.getUsers();
        Assert.assertTrue("Retrieved users should contain all predefined users",
                retrievedUsers.containsAll(predefinedUsers));

        addPayloadToReport("Retrieved Users", retrievedUsers);
    }

    @Test
    @Issue("User Retrieval")
    @Step("Get all users older than a certain age")
    public void testGetUsersOlderThan() {
        int olderThan = 30;
        CloseableHttpResponse response = userClient.getUsers(olderThan, null, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(olderThan, null, null);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertTrue("User age should be greater than " + olderThan, user.getAge() > olderThan);
        }

        addPayloadToReport("Users Older Than " + olderThan, filteredUsers);
    }

    @Test
    @Issue("User Retrieval")
    @Step("Get all users younger than a certain age")
    public void testGetUsersYoungerThan() {

        int youngerThan = 25;
        CloseableHttpResponse response = userClient.getUsers(null, null, youngerThan);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(null, null, youngerThan);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertTrue("User age should be less than " + youngerThan, user.getAge() < youngerThan);
        }

        addPayloadToReport("Users Younger Than " + youngerThan, filteredUsers);
    }

    @Test
    @Issue("User Retrieval")
    @Step("Get all users filtered by sex parameter")
    public void testGetUsersBySex() {
        String sex = "FEMALE";
        CloseableHttpResponse response = userClient.getUsers(null, sex, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(null, sex, null);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertEquals("User sex should match the filter", sex, user.getSex());
        }

        addPayloadToReport("Users Filtered by Sex: " + sex, filteredUsers);
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}