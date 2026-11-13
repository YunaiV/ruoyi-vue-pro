package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import cn.hutool.core.io.IoUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * 视频客户端契约测试用的本地 HTTP 服务：记录请求，按顺序返回预设响应，可延迟响应以模拟超时
 *
 * @author 芋道源码
 */
public class VideoMockServer implements AutoCloseable {

    /**
     * 收到的请求
     */
    public static final class Request {

        private final String method;
        private final String path;
        private final String query;
        /**
         * 请求头，key 为小写
         */
        private final Map<String, String> headers;
        private final byte[] body;

        Request(String method, String path, String query, Map<String, String> headers, byte[] body) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.headers = headers;
            this.body = body;
        }

        public String method() {
            return method;
        }

        public String path() {
            return path;
        }

        public String query() {
            return query;
        }

        public String header(String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }

        public Map<String, String> headers() {
            return headers;
        }

        public byte[] body() {
            return body;
        }

        public String bodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }

    }

    /**
     * 预设响应
     */
    public static final class Response {

        private final int status;
        private final byte[] body;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private long delayMillis;
        private boolean disconnect;

        public Response(int status, byte[] body) {
            this.status = status;
            this.body = body;
        }

        public static Response json(int status, String json) {
            return new Response(status, json.getBytes(StandardCharsets.UTF_8)).header("Content-Type", "application/json");
        }

        public Response header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * 延迟响应，用于模拟超时
         */
        public Response delay(long millis) {
            this.delayMillis = millis;
            return this;
        }

        /**
         * 收到请求后不返回任何响应、直接断开连接，用于模拟请求已发出但连接中断
         */
        public static Response disconnect() {
            Response response = new Response(0, new byte[0]);
            response.disconnect = true;
            return response;
        }

    }

    private final HttpServer server;
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final Deque<Response> responses = new ArrayDeque<>();

    public VideoMockServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "video-mock-server");
            thread.setDaemon(true);
            return thread;
        }));
        server.createContext("/", this::handle);
        server.start();
    }

    public String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public String hostAndPort() {
        return "127.0.0.1:" + server.getAddress().getPort();
    }

    public List<Request> requests() {
        return requests;
    }

    public Request lastRequest() {
        return requests.get(requests.size() - 1);
    }

    /**
     * 追加一个预设响应，按顺序使用；用完后返回 599
     */
    public VideoMockServer enqueue(Response response) {
        synchronized (responses) {
            responses.addLast(response);
        }
        return this;
    }

    public VideoMockServer enqueueJson(int status, String json) {
        return enqueue(Response.json(status, json));
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            exchange.getRequestHeaders().forEach((name, values) ->
                    headers.put(name.toLowerCase(Locale.ROOT), String.join(",", values)));
            byte[] body = IoUtil.readBytes(exchange.getRequestBody());
            requests.add(new Request(exchange.getRequestMethod(), exchange.getRequestURI().getRawPath(),
                    exchange.getRequestURI().getRawQuery(), headers, body));
            Response response;
            synchronized (responses) {
                response = responses.pollFirst();
            }
            if (response == null) {
                response = Response.json(599, "{\"mock\":\"no response\"}");
            }
            if (response.delayMillis > 0) {
                try {
                    Thread.sleep(response.delayMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (response.disconnect) {
                // 未发送响应头时 close 会直接关闭连接
                return;
            }
            response.headers.forEach((name, value) -> exchange.getResponseHeaders().set(name, value));
            exchange.sendResponseHeaders(response.status, response.body.length == 0 ? -1 : response.body.length);
            if (response.body.length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.body);
                }
            }
        } catch (IOException ignored) {
            // 客户端超时断开
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
