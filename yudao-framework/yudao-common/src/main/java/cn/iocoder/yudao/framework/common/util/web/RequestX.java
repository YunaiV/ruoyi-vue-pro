package cn.iocoder.yudao.framework.common.util.web;

import cn.hutool.http.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
    @Builder.Default
    private ContentType contentType=ContentType.JSON;
    private Object payload;

    @Getter
    @AllArgsConstructor
    public enum Method {
        GET("GET"),
        POST("POST"),;
        private final String value;
    }


}

