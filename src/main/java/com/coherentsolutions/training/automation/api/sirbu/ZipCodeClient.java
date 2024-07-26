package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import com.coherentsolutions.training.automation.api.sirbu.Utils.NoResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ZipCodeClient {

    private final ObjectMapper objectMapper;
    private final AuthProvider authProvider;

    @Getter
    private final String zipCodesUrl;
    @Getter
    private final String zipCodesExpandUrl;

    public ZipCodeClient() {
        this.objectMapper = new ObjectMapper();
        this.authProvider = AuthProvider.getInstance();
        this.zipCodesUrl = ConfigLoader.getProperty("zip.code.url");
        this.zipCodesExpandUrl = ConfigLoader.getProperty("zip.code.expand.url");
    }

    public Response getZipCodesResponse() throws NoResponseException {
        String token = authProvider.getReadToken();

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get(zipCodesUrl);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        return response;
    }

    public List<String> getZipCodes() throws IOException, NoResponseException {
        String token = authProvider.getReadToken();

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get(zipCodesUrl);

        if (response == null) {
            throw new NoResponseException("No response received from the server");
        }

        String responseBody = response.getBody().asString();
        return objectMapper.readValue(responseBody, List.class);
    }

    @SneakyThrows
    public Response postZipCodes(List<String> requestBody) {
        String token = authProvider.getWriteToken();

        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(requestBody))
                .post(zipCodesExpandUrl);
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
