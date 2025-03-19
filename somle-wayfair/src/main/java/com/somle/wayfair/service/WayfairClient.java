package com.somle.wayfair.service;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.wayfair.model.WayfairToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class WayfairClient {

    private final WayfairToken token;

    private String accessToken;

    private final String url;

    public WayfairClient(WayfairToken token) {
        this.token = token;
        this.url = "https://api.wayfair.com/";
        refreshAccessToken();
    }

    @SneakyThrows
    public void refreshAccessToken() {
        OkHttpClient client = new OkHttpClient();

        // Create the JSON body using JSONObject
        JSONObject json = new JSONObject();
        json.put("client_id", token.getClientId());
        json.put("client_secret", token.getClientSecret());
        json.put("audience", url);
        json.put("grant_type", "client_credentials");

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        // Build the request
        Request request = new Request.Builder()
                .url("https://sso.auth.wayfair.com/oauth/token")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        Response response = client.newCall(request).execute();

        accessToken = WebUtils.parseResponse(response, JSONObject.class).getString("access_token");

    }

    @SneakyThrows
    public String getOrders() {
        OkHttpClient client = new OkHttpClient();

        ClassPathResource resource = new ClassPathResource("order.graphql");
        String graphqlQuery =  new String(Files.readAllBytes(Paths.get(resource.getURI())));
        log.info(graphqlQuery);

        // Create the request body
        String jsonBody = "{\"query\": \"" + graphqlQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));


        // Build the HTTP request
        Request request = new Request.Builder()
                .url("https://api.wayfair.com/v1/graphql")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseBody = response.body().string();
        return responseBody;
    }




}
