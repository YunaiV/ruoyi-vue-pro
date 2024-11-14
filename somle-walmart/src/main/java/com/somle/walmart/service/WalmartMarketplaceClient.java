package com.somle.walmart.service;


import com.somle.walmart.model.WalmartToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;

// https://developer.walmart.com/api/us/mp/auth
@Slf4j
public class WalmartMarketplaceClient extends WalmartClient{

    public WalmartMarketplaceClient(WalmartToken token) {
        super(token);
    }

    @Override
    protected Headers headers() {
        return normalHeaders().newBuilder()
                .add("WM_SVC.NAME", token.getSvcName())
                .build();
    }

    @Override
    protected HttpUrl url(String endpoint) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("marketplace.walmartapis.com")
                .addPathSegments(endpoint)
                .build();
    }


}