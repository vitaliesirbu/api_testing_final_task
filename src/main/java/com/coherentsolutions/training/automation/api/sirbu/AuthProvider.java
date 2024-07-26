package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Base64;

public class AuthProvider {
    private static AuthProvider instance;
    private String writeToken;
    private String readToken;

    private AuthProvider() {}

    public static synchronized AuthProvider getInstance() {
        if (instance == null) {
            instance = new AuthProvider();
        }
        return instance;
    }

    public String getWriteToken() {
        if (writeToken == null) {
            writeToken = requestToken("write");
        }
        return writeToken;
    }

    public String getReadToken() {
        if (readToken == null) {
            readToken = requestToken("read");
        }
        return readToken;
    }

    private String requestToken(String scope) {
        String clientId = ConfigLoader.getProperty("clientId");
        String clientSecret = ConfigLoader.getProperty("clientSecret");
        String tokenUrl = ConfigLoader.getProperty("token.url");
        String auth = clientId + ":" + clientSecret;
        String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        Response response = RestAssured.given()
                .header("Authorization", "Basic " + encodeAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials&scope=" + scope)
                .post(tokenUrl);

        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("access_token");
        } else {
            throw new RuntimeException("Failed to get token. Status code: " + response.getStatusCode());
        }
    }
}
