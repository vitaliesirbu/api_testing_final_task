package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Data.UserUpdateDTO;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.coherentsolutions.training.automation.api.sirbu.Utils.UserDataGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Attachment;
import lombok.SneakyThrows;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserClient {

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;
    private final AuthProvider authProvider;
    private final String usersUrl;
    private final String usersUploadUrl;

    public UserClient(){
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.authProvider = AuthProvider.getInstance();
        this.usersUrl = ConfigLoader.getProperty("user.url");
        this.usersUploadUrl = ConfigLoader.getProperty("users.url");
    }

    @SneakyThrows
    public CloseableHttpResponse createUser(User user) {
        String token = authProvider.getWriteToken();
        HttpPost post = new HttpPost(usersUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(user), "UTF-8"));

        addPayloadToReport("User creation payload", user);

        return client.execute(post);
    }

    @SneakyThrows
    public CloseableHttpResponse uploadUsers(File jsonFile) {
        String token = authProvider.getWriteToken();
        HttpPost post = new HttpPost(usersUploadUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file",
                jsonFile,
                ContentType.APPLICATION_JSON,
                jsonFile.getName()
        );

        HttpEntity multipart = builder.build();
        post.setEntity(multipart);

        return client.execute(post);
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

    public User generateUserWIthoutRequiredField(){

        User user = UserDataGenerator.generateUniqueUserDataWithOutRequiredFields();

        return user;
    }

    @SneakyThrows
    public CloseableHttpResponse getUsers(Integer olderThan, String sex, Integer youngerThan) {
        if (olderThan != null && youngerThan != null) {
            CloseableHttpResponse conflictResponse = (CloseableHttpResponse) new BasicHttpResponse(
                    new BasicStatusLine(
                            new ProtocolVersion("HTTP", 1, 1),
                            HttpStatus.SC_CONFLICT,
                            "Conflict"
                    )
            );
            conflictResponse.setEntity(new StringEntity("Parameters youngerThan and olderThan can't be specified together"));
            return conflictResponse;
        }

        String token = authProvider.getReadToken();

        URIBuilder builder = new URIBuilder(usersUrl);
        List<NameValuePair> params = new ArrayList<>();

        if (olderThan != null) {
            params.add(new BasicNameValuePair("olderThan", olderThan.toString()));
        }
        if (sex != null && !sex.isEmpty()) {
            params.add(new BasicNameValuePair("sex", sex));
        }
        if (youngerThan != null) {
            params.add(new BasicNameValuePair("youngerThan", youngerThan.toString()));
        }

        builder.addParameters(params);
        URI uri = builder.build();

        HttpGet get = new HttpGet(uri);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return client.execute(get);
    }

    @SneakyThrows
    public List<User> getUsersList(Integer olderThan, String sex, Integer youngerThan) {
        CloseableHttpResponse response = getUsers(olderThan, sex, youngerThan);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

        if (statusCode == HttpStatus.SC_CONFLICT) {
            throw new IllegalArgumentException(responseBody);
        } else if (statusCode != HttpStatus.SC_OK) {
            throw new RuntimeException("Unexpected status code: " + statusCode + ". Response: " + responseBody);
        }

        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
    }

    public List<User> getUsers() {
        return getUsersList(null, null, null);
    }
    public void close() throws IOException {
        client.close();
    }

    public void createUsers(List<User> users) {
        for (User user : users) {
            createUser(user);
        }
    }
    @SneakyThrows
    public CloseableHttpResponse updateUser(UserUpdateDTO updateDTO) {
        String token = authProvider.getWriteToken();
        HttpPut put = new HttpPut(usersUrl);
        put.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity(objectMapper.writeValueAsString(updateDTO), "UTF-8"));

        return client.execute(put);
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
    public CloseableHttpResponse deleteUser(User user) {
        String token = authProvider.getWriteToken();

        HttpEntityEnclosingRequestBase delete = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "DELETE";
            }
        };
        delete.setURI(new URI(usersUrl));

        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        delete.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("age", user.getAge());
        requestBody.put("name", user.getName());
        requestBody.put("sex", user.getSex());
        requestBody.put("zipCode", user.getZipCode());

        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody));
        delete.setEntity(entity);

        return client.execute(delete);
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}
