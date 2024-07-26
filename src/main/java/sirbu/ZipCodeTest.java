package sirbu;

import com.coherentsolutions.training.automation.api.sirbu.AuthProvider;
import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import com.coherentsolutions.training.automation.api.sirbu.ZipCodeClient;
import io.qameta.allure.Attachment;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class ZipCodeTest {

    private ZipCodeClient zipCodeClient;
    @Before
    public void setUp() {
        zipCodeClient = new ZipCodeClient();
    }


    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Get all available zip codes")
    public void testGetZipCodes() {
        Response response = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl());

        int statusCode = response.getStatusCode();
        Assert.assertEquals(200, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = response.jsonPath().getList("");

        addPayloadToReport("Zip Codes List", zipCodesList);

        Assert.assertTrue(!zipCodesList.isEmpty());
    }


    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Add new zip codes")
    public void testPostZipCodes() {
        List<String> availableZipCodes = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }

        addPayloadToReport("Request Body", requestBody);

        Response response = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getWriteToken())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(zipCodeClient.getZipCodesExpandUrl());

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        for (String zipCode : requestBody) {
            Assert.assertTrue(zipCodesList.contains(zipCode));
        }
    }


    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Add duplicated zip codes")
    public void testExpandZipCodesWithDuplications() {
        List<String> availableZipCodes = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }
        requestBody.add(requestBody.get(0));

        addPayloadToReport("Request Body", requestBody);

        Response response = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getWriteToken())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(zipCodeClient.getZipCodesExpandUrl());

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        for (String zipCode : requestBody) {
            Assert.assertTrue(zipCodesList.contains(zipCode));
        }
    }

    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Check that no duplications between available zip codes and already used zip codes are added")
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {
        List<String> availableZipCodes = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }
        requestBody.add(requestBody.get(0));

        addPayloadToReport("Request Body", requestBody);

        Response response = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getWriteToken())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(zipCodeClient.getZipCodesExpandUrl());

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = given()
                .header("Authorization", "Bearer " + AuthProvider.getInstance().getReadToken())
                .get(zipCodeClient.getZipCodesUrl())
                .jsonPath()
                .getList("");

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        Assert.assertTrue(zipCodesList.contains(requestBody.get(0)));
        Assert.assertTrue(zipCodesList.contains(requestBody.get(1)));
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}
