package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 视频客户端共用的 HTTP 发送器
 *
 * 1. 传输层不自动重试（关闭 Reactor Netty 的「连接中断后重发一次」）、不跟随重定向：付费的创建请求只发一次
 * 2. 每次请求有超时；响应体超过上限直接报错，不会无界缓存
 * 3. 非 2xx 不抛异常，由各服务商按自己的错误格式解析；超时、网络异常转换为 {@link AiVideoException}，
 *    「创建任务」请求在发出后超时或中断，标记为结果未知，调用方不能自动重新提交
 * 4. 不记录请求头、请求体与完整地址；异常消息中不含凭证
 *
 * @author 芋道源码
 */
public class AiVideoHttpClient {

    /**
     * 单次请求（创建、查询）的默认超时
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    /**
     * 默认的 JSON 响应体上限
     */
    public static final int DEFAULT_MAX_RESPONSE_SIZE = 2 * 1024 * 1024;
    /**
     * 建立连接的超时
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    /**
     * HTTP 响应：状态、响应头、有界读取的响应体
     */
    @Getter
    @AllArgsConstructor
    public static class HttpResult {

        private final int status;
        private final HttpHeaders headers;
        private final byte[] body;

        public boolean is2xx() {
            return status >= 200 && status < 300;
        }

        @Override
        public String toString() {
            return "HttpResult{status=" + status + ", bodyLength=" + (body == null ? 0 : body.length) + "}";
        }

    }

    private final String platform;
    private final WebClient webClient;
    private final Duration timeout;
    private final String[] secrets;

    /**
     * @param platform 平台标识，用于异常信息
     * @param timeout 单次请求超时，为空时使用 {@link #DEFAULT_TIMEOUT}
     * @param maxResponseSize 响应体上限（字节），不大于 0 时使用 {@link #DEFAULT_MAX_RESPONSE_SIZE}
     * @param secrets 需要在错误信息中打码的凭证
     */
    public AiVideoHttpClient(String platform, Duration timeout, int maxResponseSize, String... secrets) {
        this.platform = platform;
        this.timeout = timeout != null ? timeout : DEFAULT_TIMEOUT;
        this.secrets = secrets;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(
                        maxResponseSize > 0 ? maxResponseSize : DEFAULT_MAX_RESPONSE_SIZE))
                .build();
    }

    private static HttpClient buildHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .disableRetry(true)
                .followRedirect(false);
    }

    public Duration getTimeout() {
        return timeout;
    }

    /**
     * 发送请求
     *
     * @param method 请求方法
     * @param uri 完整地址
     * @param headers 请求头（含鉴权），允许为空
     * @param body 请求体，允许为空
     * @param action 动作描述，用于异常信息，例如「提交 MiniMax 视频任务」
     * @param submit 是否为创建任务（付费）请求：是时，超时、网络中断标记为结果未知
     */
    public HttpResult execute(HttpMethod method, URI uri, Map<String, String> headers, byte[] body,
                              String action, boolean submit) {
        WebClient.RequestBodySpec spec = webClient.method(method).uri(uri);
        if (headers != null) {
            spec.headers(httpHeaders -> headers.forEach(httpHeaders::set));
        }
        WebClient.RequestHeadersSpec<?> request = body != null ? spec.bodyValue(body) : spec;
        Mono<HttpResult> mono = request.exchangeToMono(AiVideoHttpClient::readResult);
        try {
            HttpResult result = mono.timeout(timeout).block();
            if (result == null) {
                throw new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, null, null, null,
                        submit, action + "失败：没有响应");
            }
            return result;
        } catch (AiVideoException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw convertException(Exceptions.unwrap(ex), action, submit);
        }
    }

    private static Mono<HttpResult> readResult(ClientResponse response) {
        int status = response.statusCode().value();
        HttpHeaders headers = response.headers().asHttpHeaders();
        return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0])
                .map(body -> new HttpResult(status, headers, body));
    }

    private AiVideoException convertException(Throwable cause, String action, boolean submit) {
        String unknownHint = submit ? "，任务是否已创建未知，请勿自动重新提交" : "";
        if (cause instanceof TimeoutException) {
            return new AiVideoException(platform, AiVideoException.Kind.TIMEOUT, null, null, null, submit,
                    StrUtil.format("{}超时（{} 秒）{}", action, timeout.getSeconds(), unknownHint));
        }
        if (cause instanceof DataBufferLimitException) {
            return new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, null, null, null, submit,
                    StrUtil.format("{}失败：响应超过大小上限{}", action, unknownHint));
        }
        if (cause instanceof WebClientRequestException) {
            Throwable root = cause.getCause() != null ? cause.getCause() : cause;
            // 连接被拒绝：请求没有发出，可以确定任务未创建
            boolean notSent = root instanceof ConnectException;
            return new AiVideoException(platform, AiVideoException.Kind.NETWORK, null, null, null, submit && !notSent,
                    StrUtil.format("{}失败：网络异常（{}）{}", action, root.getClass().getSimpleName(),
                            notSent ? "" : unknownHint));
        }
        return new AiVideoException(platform, AiVideoException.Kind.NETWORK, null, null, null, submit,
                StrUtil.format("{}失败：{}{}", action, AiVideoUtils.sanitize(cause.getMessage(), secrets), unknownHint));
    }

    /**
     * 有界下载视频到本地文件：不携带任何鉴权头（服务商返回的是自带签名的临时地址），不跟随重定向，超时与大小受限
     *
     * 只用于下载服务商返回的结果地址；不要用于需要凭证的地址（例如 gs://），避免把密钥转发到其它域名
     *
     * @param url 视频地址，只允许 https（测试时允许 http）
     * @param target 目标文件，不存在时创建，存在时覆盖
     * @param maxBytes 最大字节数
     * @param timeout 整体超时
     * @return 实际字节数
     */
    public static long downloadToFile(String url, Path target, long maxBytes, Duration timeout) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("视频地址不正确");
        }
        String scheme = StrUtil.nullToEmpty(uri.getScheme()).toLowerCase();
        if (!("https".equals(scheme) || "http".equals(scheme)) || StrUtil.isEmpty(uri.getHost())) {
            throw new IllegalArgumentException("视频地址只支持 http(s)");
        }
        WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(buildHttpClient())).build();
        AtomicLong size = new AtomicLong();
        try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            Mono<Integer> mono = client.get().uri(uri).exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.releaseBody().then(Mono.error(new IllegalStateException(
                            "下载视频失败（HTTP " + response.statusCode().value() + "）")));
                }
                Flux<DataBuffer> flux = response.bodyToFlux(DataBuffer.class).map(buffer -> {
                    if (size.addAndGet(buffer.readableByteCount()) > maxBytes) {
                        DataBufferUtils.release(buffer);
                        throw new IllegalStateException("下载视频失败：超过大小上限 " + maxBytes + " 字节");
                    }
                    return buffer;
                });
                return DataBufferUtils.write(flux, out).map(DataBufferUtils::release).then(Mono.just(1));
            });
            mono.timeout(timeout).block();
        } catch (IOException ex) {
            throw new IllegalStateException("写入视频文件失败：" + ex.getClass().getSimpleName());
        } catch (RuntimeException ex) {
            Throwable cause = Exceptions.unwrap(ex);
            if (cause instanceof TimeoutException) {
                throw new IllegalStateException("下载视频超时");
            }
            if (cause instanceof IllegalStateException) {
                throw (IllegalStateException) cause;
            }
            throw new IllegalStateException("下载视频失败：" + cause.getClass().getSimpleName());
        }
        return size.get();
    }

}
