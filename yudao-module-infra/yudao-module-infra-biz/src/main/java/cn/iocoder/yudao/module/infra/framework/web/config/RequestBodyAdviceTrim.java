package cn.iocoder.yudao.module.infra.framework.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用springMVC的RequestBodyAdvice埋点，全局清理请求体里的字符类型的前后空格、换行符等
 *
 * @author keyang
 */
@Slf4j
@SuppressWarnings("unchecked")
@ControllerAdvice
public class RequestBodyAdviceTrim implements RequestBodyAdvice {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(@NotNull MethodParameter methodParameter, @NotNull Type type, @NotNull Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object handleEmptyBody(Object body, @NotNull HttpInputMessage httpInputMessage, @NotNull MethodParameter methodParameter, @NotNull Type type, @NotNull Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    /**
     * 重写RequestBody读取前的方法
     *
     * @param httpInputMessage httpInputMessage
     * @param methodParameter methodParameter
     * @param type type
     * @param aClass aClass
     * @return HttpInputMessage
     * @throws IOException IOException
     */
    @Override
    public @NotNull HttpInputMessage beforeBodyRead(@NotNull HttpInputMessage httpInputMessage, @NotNull MethodParameter methodParameter, @NotNull Type type, @NotNull Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        return new TrimSpaceHttpInputMessage(httpInputMessage, objectMapper);
    }

    @Override
    public @NotNull Object afterBodyRead(@NotNull Object body, @NotNull HttpInputMessage httpInputMessage, @NotNull MethodParameter methodParameter, @NotNull Type type, @NotNull Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    /**
     * 自定义请求体trim实现
     */
    static class TrimSpaceHttpInputMessage implements HttpInputMessage {

        private final HttpInputMessage origin;
        private final ObjectMapper objectMapper;

        public TrimSpaceHttpInputMessage(HttpInputMessage httpInputMessage, ObjectMapper objectMapper) {
            this.origin = httpInputMessage;
            this.objectMapper = objectMapper;
        }

        @Override
        public @NotNull HttpHeaders getHeaders() {
            return origin.getHeaders();
        }

        @Override
        public @NotNull InputStream getBody() throws IOException {
            HttpHeaders headers = origin.getHeaders();
            MediaType contentType = headers.getContentType();
            InputStream body = origin.getBody();
            if (contentType == null) {
                return body;
            }
            // 仅处理json格式的请求体
            if (!contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                return body;
            }
            // 字符编码
            Charset requestCharset = Optional.ofNullable(contentType.getCharset()).orElse(StandardCharsets.UTF_8);
            // 这里要先转string读取
            String requestBodyString = IOUtils.toString(body, requestCharset);
            log.debug("ready to clear spaces in the request body, {}", requestBodyString);
            try {
                // 反序列化
                Object param = objectMapper.readValue(requestBodyString, Object.class);
                // 核心trim逻辑
                this.translateBody(param);
                // 返回io流
                log.debug("clear spaces in the request body is success, {}", objectMapper.writeValueAsString(param));
                return new ByteArrayInputStream(objectMapper.writeValueAsBytes(param));
            } catch (Exception e) {
                log.warn("clear spaces in the request body was error, {}", e.getMessage());
                // 出现异常时，因为body已经被读取过，直接返回原body会抛stream close，所以要将requestBodyString转流抛出
                return IOUtils.toInputStream(requestBodyString, requestCharset.name());
            }
        }

        /**
         * requestBody转换
         *
         * @param param param
         * @return
         */
        private void translateBody(Object param) {
            if (param instanceof Map) {
                this.trimJsonObject(((Map<?, ?>) param));
            } else if (param instanceof List) {
                this.trimJsonArray(((List<?>) param));
            } else {
                log.warn("unknow type，{}", param.getClass());
            }
        }

        /**
         * Map转换
         *
         * @param jsonObject jsonObject
         * @return
         */
        private <M, N> void trimJsonObject(Map<?, M> jsonObject) {
            jsonObject.entrySet().forEach(entry -> {
                Object v = entry.getValue();
                if (v instanceof String) {
                    v = StringUtils.trim(((String) v));
                } else if (v instanceof Map) {
                    this.trimJsonObject(((Map<?, M>) v));
                } else if (v instanceof List) {
                    this.trimJsonArray(((List<N>) v));
                } else {
                    log.warn("unknown type，{}", v.getClass());
                }
                entry.setValue((M) v);
            });
        }

        /**
         * 数组转换
         * @param jsonArray jsonArray
         * @return
         */
        private <M, N> void trimJsonArray(List<N> jsonArray) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Object v = jsonArray.get(i);
                if (v instanceof String) {
                    jsonArray.set(i, (N) StringUtils.trim(((String) v)));
                } else if (v instanceof Map) {
                    this.trimJsonObject(((Map<?, M>) v));
                } else if (v instanceof List) {
                    this.trimJsonArray(((List<N>) v));
                } else {
                    log.warn("unknown type，{}", v.getClass());
                }
            }
        }
    }

}
