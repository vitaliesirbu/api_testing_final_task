package com.coherentsolutions.training.automation.api.sirbu;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class AuthProvider {
    private static AuthProvider instance;
    private String writeToken;
    private String readToken;

    private final String clientId = "0oa157tvtugfFXEhU4x7";
    private final String clientSecret = "X7eBCXqlFC7x-mjxG5H91IRv_Bqe1oq7ZwXNA8aq";
    private final String tokenUrl = "http://localhost:4445/oauth/token";
    private AuthProvider(){
    }

    public static synchronized  AuthProvider getInstance(){
        if (instance == null){
            instance = new AuthProvider();
        }
        return instance;
    }

    public String getWriteToken() throws IOException, InterruptedException{
        if (writeToken == null){
            writeToken = requestToken("write");
        }
        return writeToken;
    }
    public String getReadToken() throws  IOException, InterruptedException{
        if (readToken == null){
            readToken = requestToken("read");
        }
        return readToken;
    }

    private String requestToken(String scope) throws IOException, InterruptedException{
        String auth = clientId + ":" + clientSecret;
        String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodeAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=" + scope))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200){
            String responseBody = response.body();
            return extractTokenFromResponse(responseBody);
        } else {
            throw new RuntimeException("Failed  to get token: " + response.body());
        }
    }

    private String extractTokenFromResponse(String responseBody){
        int startIndex = responseBody.indexOf("\"access_token\":\"") + 16;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex,endIndex);
    }
}