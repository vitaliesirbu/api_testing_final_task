package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coherentsolutions.training.automation.api.sirbu.Utils.Constants.NUMBER_OF_ZIP_CODES;

public class ZipCodeTest {

    @Test
    @SneakyThrows
    public void testGetZipCodes() {
        ZipCodeClient zipCodeClient = new ZipCodeClient();

        AuthProvider authProvider = AuthProvider.getInstance();
        String readToken = authProvider.getReadToken();

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");

        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl, readToken);
        System.out.println("Current zip codes are: " + zipCodesList);

        Assert.assertTrue(!zipCodesList.isEmpty());

        zipCodeClient.close();
    }

    @Test
    @SneakyThrows
    public void testPostZipCodes() {
        ZipCodeClient zipCodeClient = new ZipCodeClient();

        AuthProvider authProvider = AuthProvider.getInstance();
        String writeToken = authProvider.getWriteToken();
        String readToken = authProvider.getReadToken();

        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, writeToken, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl, readToken);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));

        zipCodeClient.close();
    }

    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplications() {
        ZipCodeClient zipCodeClient = new ZipCodeClient();

        AuthProvider authProvider = AuthProvider.getInstance();
        String writeToken = authProvider.getWriteToken();
        String readToken = authProvider.getReadToken();

        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, writeToken, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl, readToken);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());

        zipCodeClient.close();
    }

    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {
        ZipCodeClient zipCodeClient = new ZipCodeClient();

        AuthProvider authProvider = AuthProvider.getInstance();
        String writeToken = authProvider.getWriteToken();
        String readToken = authProvider.getReadToken();

        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, writeToken, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl, readToken);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());

        zipCodeClient.close();
    }
}
