package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
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
        ZipCodeClient zipCodeClient = new ZipCodeClient();

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");

        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl);
        System.out.println("Current zip codes are: " + zipCodesList);

        Assert.assertTrue(!zipCodesList.isEmpty());

    }

    @Test
    @SneakyThrows
    public void testPostZipCodes() {
        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
    }


    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplications() {
        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
    }

    @Test
    @SneakyThrows
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {
        String zipCodesExpandUrl = ConfigLoader.getProperty("zipCodesExpandUrl");
        List<String> requestBody = List.of("12345", "67890", "12345");

        CloseableHttpResponse response = zipCodeClient.postZipCodes(zipCodesExpandUrl, requestBody.toString());

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String zipCodesUrl = ConfigLoader.getProperty("zipCodesUrl");
        List<String> zipCodesList = zipCodeClient.getZipCodes(zipCodesUrl);
        Set<String> zipCodesSet = new HashSet<>(zipCodesList);

        Assert.assertTrue(zipCodesSet.contains("12345"));
        Assert.assertTrue(zipCodesSet.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesSet.size());
    }
}
