package com.somle.walmart.service;


import com.somle.walmart.model.WalmartToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;

// https://developer.walmart.com/doc/us/us-supplier/us-supplier-getstarted/
@Slf4j
public class WalmartDsvClient extends WalmartClient {

    private String shipNode;

    public WalmartDsvClient(WalmartToken token, String shipNode) {
        super(token);
        this.shipNode = shipNode;
    }

    @Override
    protected Headers headers() {
        return normalHeaders().newBuilder()
            .add("WM_CONSUMER.CHANNEL.TYPE", token.getConsumerChannelType())
            .build();
    }

    @Override
    protected HttpUrl url(String endpoint) {
        return new HttpUrl.Builder()
            .scheme("https")
            .host("api-gateway.walmart.com")
            .addPathSegments(endpoint)
            .addQueryParameter("shipNode", shipNode)
            .build();
    }


}