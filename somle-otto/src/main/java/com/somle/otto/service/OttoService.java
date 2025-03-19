package com.somle.otto.service;

import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.otto.model.pojo.OttoAuthToken;
import com.somle.otto.model.resp.OttoAccessTokenResp;
import com.somle.otto.repository.OttoAccountDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
public class OttoService {

    @Autowired
    @Qualifier("ottoAccountDao")
    OttoAccountDao accountDao;

    // 刷新令牌
    public void refreshToken(OttoClient client) {
        OttoAccessTokenResp token = getToken(client);

        OttoAuthToken authToken = OttoAuthToken.builder()
                .scope(token.getScope())
                .accessToken(token.getAccessToken())
                .tokenType(token.getTokenType())
                .expiresIn(token.getExpiresIn())
                .build();

        log.debug("Generated authToken: {}", authToken);
        client.ottoAccount.setOauthToken(authToken);
        accountDao.save(client.ottoAccount);
    }

    @SneakyThrows
    private OttoAccessTokenResp getToken(OttoClient client) {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("scope", "orders products receipts returns price-reduction shipments quantities shipping-profiles availability")
                .add("client_id", client.ottoAccount.getClientId())
                .add("client_secret", client.ottoAccount.getClientSecret())
                .build();

        Request request = new Request.Builder()
                .url("https://api.otto.market/v1/token")
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return Optional.ofNullable(response.body())
                    .map(responseBody -> {
                        try {
                            String bodyString = responseBody.string();
                            log.debug("Response body: {}", bodyString);
                            return JsonUtilsX.parseObject(bodyString, OttoAccessTokenResp.class);
                        } catch (IOException e) {
                            log.error("Error reading response body", e);
                            throw new RuntimeException("Error reading response body", e);
                        }
                    })
                    .orElseThrow(() -> {
                        log.error("Response body is null");
                        return new RuntimeException("Response body is null");
                    });
        } catch (IOException e) {
            log.error("Error during token request", e);
            throw new RuntimeException("Error during token request", e);
        }
    }
}
