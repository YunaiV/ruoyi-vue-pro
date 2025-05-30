//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
    private String requestUrl;
    private String responseBody;
    private String apiName;
    private LazopHashMap commonParams;
    private LazopHashMap queryParams;

    public String getRequestUrl() {
        return this.requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public LazopHashMap getQueryParams() {
        return this.queryParams;
    }

    public void setQueryParams(LazopHashMap queryParams) {
        this.queryParams = queryParams;
    }

    public String getApiName() {
        return this.apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public LazopHashMap getCommonParams() {
        return this.commonParams;
    }

    public void setCommonParams(LazopHashMap commonParams) {
        this.commonParams = commonParams;
    }

    public Map<String, String> getAllParams() {
        Map<String, String> params = new HashMap();
        if (this.commonParams != null && !this.commonParams.isEmpty()) {
            params.putAll(this.commonParams);
        }

        if (this.queryParams != null && !this.queryParams.isEmpty()) {
            params.putAll(this.queryParams);
        }

        return params;
    }
}
