package cn.iocoder.yudao.module.ai.framework.ai.core.reasoning;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.reactivestreams.Publisher;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 思考参数的 HTTP 层请求体改写器：把档位的思考参数作为 JSON Merge Patch 合并进请求体
 *
 * 只用于复用 {@link DeepSeekApi} 的平台（DeepSeek、豆包、混元、硅基流动、文心、智谱、星火、MiniMax、月之暗面、百川、阶跃）：
 * Spring AI 1.1.8 的 DeepSeek 请求是固定字段的 record，没有 thinking、reasoning_effort、extraBody 入口，
 * 所以在客户端实例的 RestClient / WebClient 上，对已经序列化好的 JSON 请求体做 RFC 7396 合并。
 *
 * 1. 同步：{@link ClientHttpRequestInterceptor}
 * 2. 流式：{@link ExchangeFilterFunction}，包装 {@link ClientHttpRequest#writeWith(Publisher)}
 *
 * 补丁在构造时解析，之后只读；每次合并时深拷贝补丁的值，实例可以被多个请求、工具续调用共享。
 * 任何一项不满足（不是 JSON、不是 JSON 对象、请求体为空、非 UTF-8）都抛出异常，不会发出未打补丁的请求。
 *
 * @author 芋道源码
 */
public class AiRequestBodyPatcher {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    /**
     * 只读的补丁
     */
    private final ObjectNode patch;

    public AiRequestBodyPatcher(String patchJson) {
        JsonNode node;
        try {
            node = OBJECT_MAPPER.readTree(patchJson);
        } catch (IOException e) {
            throw new IllegalArgumentException("思考补丁不是合法的 JSON", e);
        }
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("思考补丁必须是 JSON 对象");
        }
        this.patch = (ObjectNode) node;
    }

    /**
     * 给 {@link DeepSeekApi.Builder} 装上补丁；patcher 为 null 时不做任何修改，保持 SDK 默认的 RestClient / WebClient
     *
     * @param builder DeepSeekApi 构建器
     * @param patcher 补丁改写器，可为 null
     * @return builder 本身
     */
    public static DeepSeekApi.Builder configure(DeepSeekApi.Builder builder, AiRequestBodyPatcher patcher) {
        if (patcher == null) {
            return builder;
        }
        return builder.restClientBuilder(patcher.restClientBuilder())
                .webClientBuilder(patcher.webClientBuilder());
    }

    /**
     * 新建带同步拦截器的 RestClient.Builder
     *
     * 每次调用都返回新的 builder：DeepSeekApi 会在 builder 上设置 baseUrl、默认请求头，共享会互相影响
     */
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestInterceptor(new PatchInterceptor());
    }

    /**
     * 新建带流式过滤器的 WebClient.Builder
     *
     * 每次调用都返回新的 builder，原因同 {@link #restClientBuilder()}
     */
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(new PatchFilter());
    }

    /**
     * 对请求体打补丁，失败时抛出 {@link IllegalStateException}
     *
     * 特殊：不抛出 {@link IOException}，避免同步请求被包装成 ResourceAccessException 后被 Spring AI 重试
     *
     * @param headers 请求头，用于校验 Content-Type
     * @param body 原始请求体
     * @return 打补丁后的请求体（UTF-8）
     */
    public byte[] apply(HttpHeaders headers, byte[] body) {
        MediaType contentType = headers.getContentType();
        if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            throw new IllegalStateException("思考补丁只支持 JSON 请求，实际 Content-Type 为 " + contentType);
        }
        Charset charset = contentType.getCharset();
        if (charset != null && !StandardCharsets.UTF_8.equals(charset)) {
            throw new IllegalStateException("思考补丁只支持 UTF-8 请求，实际编码为 " + charset);
        }
        if (ArrayUtil.isEmpty(body)) {
            throw new IllegalStateException("思考补丁要求请求体非空");
        }
        JsonNode node;
        try {
            node = OBJECT_MAPPER.readTree(body);
        } catch (IOException e) {
            throw new IllegalStateException("思考补丁要求请求体是合法的 JSON", e);
        }
        if (node == null || !node.isObject()) {
            throw new IllegalStateException("思考补丁要求请求体是 JSON 对象");
        }
        JsonNode merged = mergePatch(node, patch);
        try {
            return OBJECT_MAPPER.writeValueAsBytes(merged);
        } catch (IOException e) {
            throw new IllegalStateException("思考补丁合并后序列化失败", e);
        }
    }

    /**
     * RFC 7396 JSON Merge Patch：补丁是对象时逐个键递归合并，值为 null 表示删除；补丁不是对象时整体替换（数组也整体替换）
     * 思考参数保存时已禁止 null，null 分支只为保持 RFC 7396 语义完整
     *
     * target 可能被原地修改；patch 不会被修改，写入 target 的值都是深拷贝
     *
     * @param target 原始值，可为 null
     * @param patch 补丁
     * @return 合并后的值
     */
    public static JsonNode mergePatch(JsonNode target, JsonNode patch) {
        if (patch == null || !patch.isObject()) {
            return patch == null ? null : patch.deepCopy();
        }
        ObjectNode result = target != null && target.isObject()
                ? (ObjectNode) target : OBJECT_MAPPER.createObjectNode();
        for (Map.Entry<String, JsonNode> field : patch.properties()) {
            if (field.getValue().isNull()) {
                result.remove(field.getKey());
                continue;
            }
            result.set(field.getKey(), mergePatch(result.get(field.getKey()), field.getValue()));
        }
        return result;
    }

    /**
     * 同步请求：RestClient 拦截器
     *
     * Spring 6.2 的 InterceptingClientHttpRequest 在拦截链末端按新请求体长度重设 Content-Length
     */
    private class PatchInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            return execution.execute(request, apply(request.getHeaders(), body));
        }

    }

    /**
     * 流式请求：WebClient 过滤器，替换请求体写出逻辑
     */
    private class PatchFilter implements ExchangeFilterFunction {

        @Override
        public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
            BodyInserter<?, ? super ClientHttpRequest> original = request.body();
            BodyInserter<Object, ClientHttpRequest> inserter = (outputMessage, context) ->
                    original.insert(new PatchRequestDecorator(outputMessage), context);
            ClientRequest patched = ClientRequest.from(request).body(inserter).build();
            return next.exchange(patched);
        }

    }

    /**
     * 拼接完整请求体 → 打补丁 → 按新长度写出；没有请求体或分段写出时直接报错
     */
    private class PatchRequestDecorator extends ClientHttpRequestDecorator {

        PatchRequestDecorator(ClientHttpRequest delegate) {
            super(delegate);
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return DataBufferUtils.join(body)
                    .switchIfEmpty(Mono.error(() -> new IllegalStateException("思考补丁要求请求体非空")))
                    .flatMap(joined -> {
                        byte[] bytes = new byte[joined.readableByteCount()];
                        try {
                            joined.read(bytes);
                        } finally {
                            DataBufferUtils.release(joined);
                        }
                        byte[] merged = apply(getHeaders(), bytes);
                        getHeaders().setContentLength(merged.length);
                        return super.writeWith(Mono.just(bufferFactory().wrap(merged)));
                    });
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return Mono.error(new IllegalStateException("思考补丁不支持分段写出的请求体"));
        }

        @Override
        public Mono<Void> setComplete() {
            return Mono.error(new IllegalStateException("思考补丁要求请求体非空"));
        }

    }

}
