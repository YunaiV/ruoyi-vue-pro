package com.somle.tiktok.sdk.invoke;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AccessTokenAPI {
    private static final String GET_TOKEN_URL = "https://auth.tiktok-shops.com/api/v2/token/get";
    private static final String REFRESH_TOKEN_URL = "https://auth.tiktok-shops.com/api/v2/token/refresh";
    private static final String GRANT_TYPE_AUTHORIZED_CODE = "authorized_code";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final Gson gson = new Gson();
    private final String appKey;
    private final String appSecret;

    public AccessTokenAPI(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    private static String doRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        Scanner scanner = new Scanner(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        return response.toString();
    }

    public ResponseInfo getToken(String authCode) throws IOException {
        String url = String.format("%s?app_key=%s&app_secret=%s&auth_code=%s&grant_type=%s",
            GET_TOKEN_URL, appKey, appSecret, authCode, GRANT_TYPE_AUTHORIZED_CODE);
        String response = doRequest(url);
        return gson.fromJson(response, ResponseInfo.class);
    }

    public ResponseInfo refreshToken(String refreshToken) throws IOException {
        String url = String.format("%s?app_key=%s&app_secret=%s&refresh_token=%s&grant_type=%s",
            REFRESH_TOKEN_URL, appKey, appSecret, refreshToken, GRANT_TYPE_REFRESH_TOKEN);
        String response = doRequest(url);
        return gson.fromJson(response, ResponseInfo.class);
    }
}
