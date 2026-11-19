package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import cn.hutool.core.io.IoUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    public static final class Request {

        private final String method;
        private final String path;
        private final String rawQuery;
        private final Map<String, List<String>> headers;
        private final String body;

        public Request(String method, String path, String rawQuery,
                       Map<String, List<String>> headers, String body) {
            this.method = method;
            this.path = path;
            this.rawQuery = rawQuery;
            this.headers = headers;
            this.body = body;
        }

        public String method() {
            return method;
        }

        public String path() {
            return path;
        }

        public String rawQuery() {
            return rawQuery;
        }

        public Map<String, List<String>> headers() {
            return headers;
        }

        public String body() {
            return body;
        }

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
    public static final class Response {

        private final int status;
        private final String contentType;
        private final String body;
        private final long delayMillis;

        public Response(int status, String contentType, String body, long delayMillis) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
            this.delayMillis = delayMillis;
        }

        public int status() {
            return status;
        }

        public String contentType() {
            return contentType;
        }

        public String body() {
            return body;
        }

        public long delayMillis() {
            return delayMillis;
        }

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
        try {
            String body = new String(IoUtil.readBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Request request = new Request(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(),
                    Collections.unmodifiableMap(new HashMap<>(exchange.getRequestHeaders())), body);
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
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
