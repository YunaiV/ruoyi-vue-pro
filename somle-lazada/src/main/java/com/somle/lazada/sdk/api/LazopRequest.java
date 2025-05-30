//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.api;


import com.somle.lazada.sdk.util.FileItem;
import com.somle.lazada.sdk.util.LazopHashMap;

import java.util.HashMap;
import java.util.Map;

public class LazopRequest {
    protected LazopHashMap apiParams;
    protected LazopHashMap headerParams;
    protected Map<String, FileItem> fileParams;
    private Long timestamp;
    private String apiName;
    private String httpMethod = "POST";

    public LazopRequest() {
    }

    public LazopRequest(String apiName) {
        this.apiName = apiName;
    }

    public void addApiParameter(String key, String value) {
        if (this.apiParams == null) {
            this.apiParams = new LazopHashMap();
        }

        this.apiParams.put(key, value);
    }

    public void addFileParameter(String key, FileItem file) {
        if (this.fileParams == null) {
            this.fileParams = new HashMap();
        }

        this.fileParams.put(key, file);
    }

    public void addHeaderParameter(String key, String value) {
        if (this.headerParams == null) {
            this.headerParams = new LazopHashMap();
        }

        this.headerParams.put(key, value);
    }

    public LazopHashMap getApiParams() {
        return this.apiParams;
    }

    public Map<String, FileItem> getFileParams() {
        return this.fileParams;
    }

    public Map<String, String> getHeaderParams() {
        return this.headerParams;
    }

    public void setHeaderParams(LazopHashMap headerParams) {
        this.headerParams = headerParams;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getApiName() {
        return this.apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
