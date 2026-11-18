package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * OCR 单元测试用的本地 HTTP 桩服务：基于 JDK HttpServer，真实走网络，
 * 记录收到的请求（方法、路径、查询串、请求头、请求体），按顺序返回预设的响应。
 *
 * 用于验证各厂商 Model（含腾讯、火山官方 SDK）真正发出的 HTTP 请求和对响应的处理。
 *
 * @author 芋道源码
 */
public class OcrStubServer implements AutoCloseable {

    /**
     * 收到的请求
     */
    public record Request(String method, String path, String rawQuery,
                          Map<String, List<String>> headers, String body) {

        public String header(String name) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue().isEmpty() ? null : entry.getValue().get(0);
                }
            }
            return null;
        }

    }

    /**
     * 预设的响应
     *
     * @param delayMillis 返回前等待的毫秒数，用于模拟超时
     */
    public record Response(int status, String contentType, String body, long delayMillis) {

        public static Response json(int status, String body) {
            return new Response(status, "application/json", body, 0);
        }

        public static Response delayed(long delayMillis) {
            return new Response(200, "application/json", "{}", delayMillis);
        }

    }

    private final HttpServer server;
    private final List<Request> requests = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentLinkedQueue<Function<Request, Response>> responses = new ConcurrentLinkedQueue<>();

    public OcrStubServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    /**
     * 追加一个响应（按请求到达顺序依次使用）
     */
    public OcrStubServer enqueue(Response response) {
        responses.add(request -> response);
        return this;
    }

    /**
     * 追加一个根据请求动态生成的响应
     */
    public OcrStubServer enqueue(Function<Request, Response> responder) {
        responses.add(responder);
        return this;
    }

    public String baseUrl() {
        return "http://" + hostAndPort();
    }

    public String hostAndPort() {
        return server.getAddress().getHostString() + ":" + server.getAddress().getPort();
    }

    public List<Request> requests() {
        return new ArrayList<>(requests);
    }

    public Request lastRequest() {
        List<Request> list = requests();
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Request request = new Request(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(), Map.copyOf(exchange.getRequestHeaders()), body);
            requests.add(request);
            Function<Request, Response> responder = responses.poll();
            Response response = responder != null ? responder.apply(request)
                    : Response.json(500, "{\"message\":\"no stub response\"}");
            if (response.delayMillis() > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(response.delayMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", response.contentType());
            exchange.sendResponseHeaders(response.status(), bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            } catch (IOException ignored) {
                // 客户端已超时断开
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
