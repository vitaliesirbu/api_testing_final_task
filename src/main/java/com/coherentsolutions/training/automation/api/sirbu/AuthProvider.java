package com.coherentsolutions.training.automation.api.sirbu;

import com.coherentsolutions.training.automation.api.sirbu.Utils.ConfigLoader;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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

    @SneakyThrows
    public String getWriteToken() {
        if (writeToken == null) {
            writeToken = requestToken("write");
        }
        return writeToken;
    }

    @SneakyThrows
    public String getReadToken() {
        if (readToken == null) {
            readToken = requestToken("read");
        }
        return readToken;
    }

    @SneakyThrows
    private String requestToken(String scope) {
        String clientId = ConfigLoader.getProperty("clientId");
        String clientSecret = ConfigLoader.getProperty("clientSecret");
        String tokenUrl = ConfigLoader.getProperty("token.url");
        String auth = clientId + ":" + clientSecret;
        String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(tokenUrl);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodeAuth);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity("grant_type=client_credentials&scope=" + scope, "UTF-8"));

        CloseableHttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            client.close();
            return extractTokenFromResponse(responseBody);
        } else {
            client.close();
            throw new RuntimeException("Failed to get token. Status code: " + statusCode);
        }
    }

    private String extractTokenFromResponse(String responseBody) {
        int startIndex = responseBody.indexOf("\"access_token\":\"") + 16;
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
}
