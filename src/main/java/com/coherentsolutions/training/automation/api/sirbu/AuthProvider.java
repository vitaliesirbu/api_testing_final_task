package com.coherentsolutions.training.automation.api.sirbu;

import java.io.IOException;
import java.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


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

    private String requestToken(String scope) throws IOException {
        String auth = clientId + ":" + clientSecret;
        String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(tokenUrl);

        post.setHeader("Authorization", "Basic " + encodeAuth);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity("grant_type=client_credentials&scope=" + scope));

        try (CloseableHttpResponse response = client.execute(post)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                return extractTokenFromResponse(responseBody);
            } else {
                throw new RuntimeException("Failed to get token: " + EntityUtils.toString(response.getEntity()));
            }
        } finally {
            client.close();
        }
    }

    private String extractTokenFromResponse(String responseBody){
        int startIndex = responseBody.indexOf("\"access_token\":\"") + 16;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex,endIndex);
    }
}