package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.coherentsolutions.training.automation.api.sirbu.Utils.Constants.NUMBER_OF_ZIP_CODES;

public class ZipCodeTest {

    @Test
    @SneakyThrows
    public void testGetZipCodes() {

        AuthProvider authProvider = AuthProvider.getInstance();
        String readToken = authProvider.getReadToken();

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(zipCodesUrl);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + readToken);

        CloseableHttpResponse response = client.execute(get);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode zipCodesNode = objectMapper.readTree(responseBody);
        System.out.println("Current zip codes are: " + zipCodesNode);

        Assert.assertTrue(zipCodesNode.isArray());
        Assert.assertTrue(!zipCodesNode.isEmpty());

        client.close();
    }

    @Test
    @SneakyThrows
    public void testPostZipCodes() {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            AuthProvider authProvider = AuthProvider.getInstance();
            String writeToken = authProvider.getWriteToken();

            String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");

            String requestBody = "[\"12345\", \"67890\"]";

            HttpPost post = new HttpPost(zipCodesExpandUrl);
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + writeToken);
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(201, statusCode);

            HttpGet get = new HttpGet(ConfigLoader.getProperty("zipCodesUrl"));
            get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authProvider.getReadToken());
            CloseableHttpResponse getResponse = client.execute(get);
            String getResponseBody = EntityUtils.toString(getResponse.getEntity(), "UTF-8");
            JsonNode zipCodesNode = new ObjectMapper().readTree(getResponseBody);
            Set<String> zipCodesSet = new HashSet<>();
            zipCodesNode.forEach(zipCode -> zipCodesSet.add(zipCode.asText()));

            Assert.assertTrue(zipCodesSet.contains("12345"));
            Assert.assertTrue(zipCodesSet.contains("67890"));
        }
    }
    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplications() {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            AuthProvider authProvider = AuthProvider.getInstance();
            String writeToken = authProvider.getWriteToken();

            String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");

            String requestBody = "[\"12345\", \"67890\", \"12345\"]";

            HttpPost post = new HttpPost(zipCodesExpandUrl);
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + writeToken);
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(201, statusCode);

            HttpGet get = new HttpGet(ConfigLoader.getProperty("zipCodesUrl"));
            get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authProvider.getReadToken());
            CloseableHttpResponse getResponse = client.execute(get);
            String getResponseBody = EntityUtils.toString(getResponse.getEntity(), "UTF-8");
            JsonNode zipCodesNode = new ObjectMapper().readTree(getResponseBody);
            Set<String> zipCodesSet = new HashSet<>();
            zipCodesNode.forEach(zipCode -> zipCodesSet.add(zipCode.asText()));

            Assert.assertTrue(zipCodesSet.contains("12345"));
            Assert.assertTrue(zipCodesSet.contains("67890"));
            Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
        }
    }

    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            AuthProvider authProvider = AuthProvider.getInstance();
            String writeToken = authProvider.getWriteToken();

            String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");

            String requestBody = "[\"12345\", \"67890\", \"12345\"]";

            HttpPost post = new HttpPost(zipCodesExpandUrl);
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + writeToken);
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            post.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(201, statusCode);

            HttpGet get = new HttpGet(ConfigLoader.getProperty("zipCodesUrl"));
            get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authProvider.getReadToken());
            CloseableHttpResponse getResponse = client.execute(get);
            String getResponseBody = EntityUtils.toString(getResponse.getEntity(), "UTF-8");
            JsonNode zipCodesNode = new ObjectMapper().readTree(getResponseBody);
            Set<String> zipCodesSet = new HashSet<>();
            zipCodesNode.forEach(zipCode -> zipCodesSet.add(zipCode.asText()));

            Assert.assertTrue(zipCodesSet.contains("12345"));
            Assert.assertTrue(zipCodesSet.contains("67890"));

            Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
        }
    }
}