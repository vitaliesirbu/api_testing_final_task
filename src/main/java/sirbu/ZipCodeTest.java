package sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ZipCodeGenerator;
import com.coherentsolutions.training.automation.api.sirbu.ZipCodeClient;
import io.qameta.allure.Attachment;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


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
        Response response = zipCodeClient.getZipCodesResponse();

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
        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }

        addPayloadToReport("Request Body", requestBody);

        Response response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> updatedZipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", updatedZipCodesList);

        for (String zipCode : requestBody) {
            Assert.assertTrue(updatedZipCodesList.contains(zipCode));
        }
    }


    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Add duplicated zip codes")
    public void testExpandZipCodesWithDuplications() {
        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }
        requestBody.add(requestBody.get(0));

        addPayloadToReport("Request Body", requestBody);

        Response response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> updatedZipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", updatedZipCodesList);

        for (String zipCode : requestBody) {
            Assert.assertTrue(updatedZipCodesList.contains(zipCode));
        }
    }

    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Check that no duplications between available zip codes and already used zip codes are added")
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {
        List<String> availableZipCodes = zipCodeClient.getZipCodes();

        List<String> requestBody = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requestBody.add(ZipCodeGenerator.generateUnavailableZipCode(availableZipCodes));
        }
        requestBody.add(requestBody.get(0));

        addPayloadToReport("Request Body", requestBody);

        Response response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = response.getBody().asString();
        addPayloadToReport("Response", responseBody);

        List<String> updatedZipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", updatedZipCodesList);

        Assert.assertTrue(updatedZipCodesList.contains(requestBody.get(0)));
        Assert.assertTrue(updatedZipCodesList.contains(requestBody.get(1)));

        int occurrences = 0;
        for (String zipCode : updatedZipCodesList) {
            if (zipCode.equals(requestBody.get(0))) {
                occurrences++;
            }
        }
        Assert.assertEquals("Duplicate zip code should not be added separately", 1, occurrences);
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}
