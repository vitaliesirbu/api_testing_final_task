package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class UserRetrievalTest {

    private UserClient userClient;

    @Before
    public void setUp() {
        userClient = new UserClient();
    }

    @After
    public void tearDown() throws Exception {
        userClient.close();
    }

    @Test
    public void testGetAllUsers() {
        CloseableHttpResponse response = userClient.getUsers(null, null, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> users = userClient.getUsersList(null, null, null);
        Assert.assertFalse("User list should not be empty", users.isEmpty());
    }

    @Test
    public void testGetUsersOlderThan() {
        int olderThan = 30;
        CloseableHttpResponse response = userClient.getUsers(olderThan, null, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(olderThan, null, null);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertTrue("User age should be greater than " + olderThan, user.getAge() > olderThan);
        }
    }

    @Test
    public void testGetUsersYoungerThan() {

        int youngerThan = 25;
        CloseableHttpResponse response = userClient.getUsers(null, null, youngerThan);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(null, null, youngerThan);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertTrue("User age should be less than " + youngerThan, user.getAge() < youngerThan);
        }
    }

    @Test
    public void testGetUsersBySex() {
        String sex = "FEMALE";
        CloseableHttpResponse response = userClient.getUsers(null, sex, null);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<User> filteredUsers = userClient.getUsersList(null, sex, null);
        Assert.assertFalse("Filtered user list should not be empty", filteredUsers.isEmpty());
        for (User user : filteredUsers) {
            Assert.assertEquals("User sex should match the filter", sex, user.getSex());
        }
    }
}