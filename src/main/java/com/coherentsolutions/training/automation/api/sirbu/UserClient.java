package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Data.User;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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
        this.usersUrl = ConfigLoader.getProperty("user.url");
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

    public void createUsers(List<User> users) throws IOException {
        for (User user : users) {
            createUser(user);
        }
    }
}
