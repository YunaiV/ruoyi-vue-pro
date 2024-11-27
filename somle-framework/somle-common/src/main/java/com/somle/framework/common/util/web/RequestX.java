package com.somle.framework.common.util.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import okhttp3.Request;

import java.util.List;
import java.util.Map;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: $
 */
@Data
@Builder
public class RequestX {
    private Method requestMethod;
    private String url;
//    private List<String> paths;
    private Object queryParams;
    private Map<String, String> headers;
    private Object payload;

    @Getter
    @AllArgsConstructor
    public enum Method {
        GET("GET"),
        POST("POST"),;
        private final String value;
    }


}

