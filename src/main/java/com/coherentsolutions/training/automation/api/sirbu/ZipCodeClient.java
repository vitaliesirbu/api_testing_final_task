package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
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
import java.util.Random;

public class ZipCodeClient {

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;
    private final AuthProvider authProvider;
    private final String zipCodesUrl;
    private final String zipCodesExpandUrl;

    public ZipCodeClient() {
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.authProvider = AuthProvider.getInstance();
        this.zipCodesUrl = ConfigLoader.getProperty("zip.code.url");
        this.zipCodesExpandUrl = ConfigLoader.getProperty("zip.code.expand.url");
    }

    public CloseableHttpResponse getZipCodesResponse() throws IOException, NoResponseException {
        String token = authProvider.getReadToken();
        HttpGet get = new HttpGet(zipCodesUrl);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        CloseableHttpResponse response = client.execute(get);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        return response;
    }
    public List<String> getZipCodes() throws IOException, NoResponseException {
        String token = authProvider.getReadToken();
        HttpGet get = new HttpGet(zipCodesUrl);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        CloseableHttpResponse response = client.execute(get);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        return objectMapper.readValue(responseBody, List.class);
    }

    @SneakyThrows
    public CloseableHttpResponse postZipCodes(List<String> requestBody) {
        String token = authProvider.getWriteToken();
        HttpPost post = new HttpPost(zipCodesExpandUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8"));

        return client.execute(post);
    }

    public void close() throws IOException {
        client.close();
    }
    public String generateNewZipCode(String oldZipCode) {
        Random random = new Random();
        String newZipCode;
        do {
            newZipCode = String.format("%05d", random.nextInt(100000));
        } while (newZipCode.equals(oldZipCode));
        return newZipCode;
    }
}