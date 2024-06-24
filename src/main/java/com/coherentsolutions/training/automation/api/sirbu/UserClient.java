package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.coherentsolutions.training.automation.api.sirbu.Utils.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class UserClient {

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;
    private final AuthProvider authProvider;
    private final String usersUrl;

    public UserClient(){
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.authProvider = AuthProvider.getInstance();
        this.usersUrl = ConfigLoader.getProperty("userUrl");
    }

    @SneakyThrows
    public CloseableHttpResponse createUser(User user) {
        String token = authProvider.getWriteToken();
        HttpPost post = new HttpPost(usersUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(user), "UTF-8"));

        return client.execute(post);
    }

    @SneakyThrows
    public List<User> getUsers() {
        String token = authProvider.getReadToken();
        HttpGet get = new HttpGet(usersUrl);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        CloseableHttpResponse response = client.execute(get);

        if (response == null){
            throw new NoResponseException("No response received from the server");
        }

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
    }

    public void close() throws IOException {
        client.close();
    }
}
