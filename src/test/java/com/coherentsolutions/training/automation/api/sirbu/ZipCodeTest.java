package com.coherentsolutions.training.automation.api.sirbu;

import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coherentsolutions.training.automation.api.sirbu.Utils.Constants.NUMBER_OF_ZIP_CODES;

public class ZipCodeTest {

    private ZipCodeClient zipCodeClient;
    @Before
    public void setUp() {
        zipCodeClient = new ZipCodeClient();
    }

    @After
    @SneakyThrows
    public void tearDown() {
        zipCodeClient.close();
    }

    @Test
    @SneakyThrows
    public void testGetZipCodes() {

        List<String> zipCodesList = zipCodeClient.getZipCodes();
        System.out.println("Current zip codes are: " + zipCodesList);

        Assert.assertTrue(!zipCodesList.isEmpty());

    }

    @Test
    @SneakyThrows
    public void testPostZipCodes() {

        List<String> requestBody = List.of("12345", "67890");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        List<String> zipCodesList = zipCodeClient.getZipCodes();
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
    }


    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplications() {

        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        List<String> zipCodesList = zipCodeClient.getZipCodes();
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
    }

    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {

        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        List<String> zipCodesList = zipCodeClient.getZipCodes();
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
    }
}
