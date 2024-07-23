
import com.coherentsolutions.training.automation.api.sirbu.ZipCodeClient;
import io.qameta.allure.Attachment;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    @Issue("Zip Code")
    @Step("Get all available zip codes")
    public void testGetZipCodes() {
        CloseableHttpResponse response = zipCodeClient.getZipCodesResponse();
        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(200, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity());
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Zip Codes List", zipCodesList);

        Assert.assertTrue(!zipCodesList.isEmpty());

    }

    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Add new zip codes")
    public void testPostZipCodes() {

        List<String> requestBody = List.of("12345", "67890");

        addPayloadToReport("Request Body", requestBody);

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity());
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        Assert.assertTrue(zipCodesList.contains("12345"));
        Assert.assertTrue(zipCodesList.contains("67890"));
    }


    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Add duplicated zip codes")
    public void testExpandZipCodesWithDuplications() {

        List<String> requestBody = List.of("12345", "67890", "12345");

        addPayloadToReport("Request Body", requestBody);

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity());
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        Assert.assertTrue(zipCodesList.contains("12345"));
        Assert.assertTrue(zipCodesList.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesList.size());
    }

    @Test
    @SneakyThrows
    @Issue("Zip Code")
    @Step("Check that no duplications between available zip codes and already used zip codes are added")
    public void testExpandZipCodesWithDuplicationsBetweenAvailableZip() {

        List<String> requestBody = List.of("12345", "67890", "12345");

        addPayloadToReport("Request Body", requestBody);

        CloseableHttpResponse response = zipCodeClient.postZipCodes(requestBody);

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(201, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity());
        addPayloadToReport("Response", responseBody);

        List<String> zipCodesList = zipCodeClient.getZipCodes();

        addPayloadToReport("Updated Zip Codes List", zipCodesList);

        Assert.assertTrue(zipCodesList.contains("12345"));
        Assert.assertTrue(zipCodesList.contains("67890"));
        Assert.assertEquals(NUMBER_OF_ZIP_CODES.intValue(), zipCodesList.size());
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String addPayloadToReport(String attachmentName, Object payload) {
        return payload.toString();
    }
}
