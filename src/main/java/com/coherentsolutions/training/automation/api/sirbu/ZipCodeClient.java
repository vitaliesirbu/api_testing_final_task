package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
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

public class ZipCodeClient {

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    public ZipCodeClient() {
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getZipCodes(String url, String token) throws IOException, NoResponseException {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        CloseableHttpResponse response = client.execute(get);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 201) {
            throw new RuntimeException("Failed to get zip codes. Status code: " + statusCode);
        }

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        return objectMapper.readValue(responseBody, List.class);
    }

    @SneakyThrows
    public CloseableHttpResponse postZipCodes(String url, String token, String requestBody) {
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(requestBody, "UTF-8"));

        return client.execute(post);
    }

    public void close() throws IOException {
        client.close();
    }
}
