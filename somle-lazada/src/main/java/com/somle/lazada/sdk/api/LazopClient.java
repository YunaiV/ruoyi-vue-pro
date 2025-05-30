//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.api;

import com.somle.lazada.sdk.util.*;
import com.somle.lazada.sdk.util.json.JSONReader;
import com.somle.lazada.sdk.util.json.JSONValidatingReader;

import java.io.IOException;
import java.net.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LazopClient implements ILazopClient {
    protected String serverUrl;
    protected String appKey;
    protected String appSecret;
    protected String signMethod;
    protected int connectTimeout;
    protected int readTimeout;
    protected boolean useGzipEncoding;
    protected Proxy proxy;
    protected String sdkVersion;
    protected String logLevel;

    public LazopClient(String serverUrl, String appKey, String appSecret) {
        this.signMethod = "sha256";
        this.connectTimeout = 15000;
        this.readTimeout = 30000;
        this.useGzipEncoding = true;
        this.sdkVersion = "lazop-sdk-java-20181207";
        this.logLevel = "ERROR";
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.serverUrl = serverUrl;
    }

    public LazopClient(String serverUrl, String appKey, String appSecret, int connectTimeout, int readTimeout) {
        this(serverUrl, appKey, appSecret);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public LazopResponse execute(LazopRequest request) throws ApiException {
        return this.execute(request, (String) null);
    }

    public LazopResponse execute(LazopRequest request, String accessToken) throws ApiException {
        return this.doExecute(request, accessToken);
    }

    private LazopResponse doExecute(LazopRequest request, String accessToken) throws ApiException {
        long start = System.currentTimeMillis();
        RequestContext requestContext = new RequestContext();
        LazopHashMap bizParams = new LazopHashMap((Map) (request.getApiParams() != null ? request.getApiParams() : new HashMap()));
        requestContext.setQueryParams(bizParams);
        requestContext.setApiName(request.getApiName());
        LazopHashMap commonParams = new LazopHashMap();
        commonParams.put("app_key", this.appKey);
        Long timestamp = request.getTimestamp();
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }

        commonParams.put("timestamp", new Date(timestamp));
        commonParams.put("sign_method", this.signMethod);
        commonParams.put("access_token", accessToken);
        commonParams.put("partner_id", this.sdkVersion);
        if (this.isDebugEnabled()) {
            commonParams.put("debug", true);
        }

        requestContext.setCommonParams(commonParams);

        try {
            commonParams.put("sign", LazopUtils.signApiRequest(requestContext, this.appSecret, this.signMethod));
            String rpcUrl = WebUtils.buildRestUrl(this.serverUrl, request.getApiName());
            String urlQuery = WebUtils.buildQuery(requestContext.getCommonParams(), "UTF-8");
            String fullUrl = WebUtils.buildRequestUrl(rpcUrl, new String[]{urlQuery});
            String rsp = null;
            if (this.useGzipEncoding) {
                request.addHeaderParameter("Accept-Encoding", "gzip");
            }

            if (request.getFileParams() != null) {
                rsp = WebUtils.doPost(fullUrl, bizParams, request.getFileParams(), request.getHeaderParams(), "UTF-8", this.connectTimeout, this.readTimeout);
            } else if (request.getHttpMethod().equals("POST")) {
                rsp = WebUtils.doPost(fullUrl, bizParams, request.getHeaderParams(), "UTF-8", this.connectTimeout, this.readTimeout, this.proxy);
            } else {
                rsp = WebUtils.doGet(fullUrl, bizParams, request.getHeaderParams(), this.connectTimeout, this.readTimeout, "UTF-8", this.proxy);
            }

            requestContext.setResponseBody(rsp);
        } catch (IOException e) {
            LazopLogger.write(this.appKey, this.sdkVersion, request.getApiName(), this.serverUrl, requestContext.getAllParams(), System.currentTimeMillis() - start, e.toString());
            throw new ApiException(e);
        } catch (Exception e) {
            LazopLogger.write(this.appKey, this.sdkVersion, request.getApiName(), this.serverUrl, requestContext.getAllParams(), System.currentTimeMillis() - start, e.toString());
            throw new ApiException(e);
        }

        LazopResponse response = this.parseResponse(requestContext.getResponseBody());
        if (!response.isSuccess()) {
            LazopLogger.write(this.appKey, this.sdkVersion, request.getApiName(), this.serverUrl, requestContext.getAllParams(), System.currentTimeMillis() - start, response.getBody());
        } else if (this.isDebugEnabled() || this.isInfoEnabled()) {
            LazopLogger.write(this.appKey, this.sdkVersion, request.getApiName(), this.serverUrl, requestContext.getAllParams(), System.currentTimeMillis() - start, "");
        }

        return response;
    }

    private LazopResponse parseResponse(String jsonRsp) {
        JSONReader reader = new JSONValidatingReader();
        Map<?, ?> root = (Map) reader.read(jsonRsp);
        LazopResponse response = new LazopResponse();
        response.setType((String) root.get("type"));
        response.setCode(String.valueOf(root.get("code")));
        response.setMessage((String) root.get("message"));
        response.setRequestId((String) root.get("request_id"));
        response.setBody(jsonRsp);
        return response;
    }

    public void setNeedEnableLogger(boolean needEnableLogger) {
        LazopLogger.setNeedEnableLogger(needEnableLogger);
    }

    public void setIgnoreSSLCheck(boolean ignore) {
        WebUtils.setIgnoreSSLCheck(ignore);
    }

    public void setUseGzipEncoding(boolean useGzipEncoding) {
        this.useGzipEncoding = useGzipEncoding;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isDebugEnabled() {
        return this.logLevel.equals("DEBUG");
    }

    public boolean isInfoEnabled() {
        return this.logLevel.equals("INFO");
    }

    public boolean isErrorEnabled() {
        return this.logLevel.equals("ERROR");
    }
}
