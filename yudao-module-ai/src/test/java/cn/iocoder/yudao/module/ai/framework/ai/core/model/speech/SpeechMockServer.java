package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import cn.hutool.core.io.IoUtil;

/**
 * 语音合成协议测试用的本地 HTTP 服务
 *
 * 基于 {@link ServerSocket} 直接读写字节，可以精确控制分块边界、提前断开、不响应（超时），
 * 客户端走真实的网络栈（WebClient / OkHttp），不访问外部服务
 *
 * @author 芋道源码
 */
public class SpeechMockServer implements AutoCloseable {

    /**
     * 收到的请求
     */
    public static final class Request {

        private final String method;
        private final String target;
        /**
         * 请求头，key 为小写
         */
        private final Map<String, String> headers;
        private final byte[] body;

        Request(String method, String target, Map<String, String> headers, byte[] body) {
            this.method = method;
            this.target = target;
            this.headers = headers;
            this.body = body;
        }

        public String method() {
            return method;
        }

        public String target() {
            return target;
        }

        public Map<String, String> headers() {
            return headers;
        }

        public byte[] body() {
            return body;
        }

        public String header(String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }

        public String bodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        public String path() {
            int index = target.indexOf('?');
            return index >= 0 ? target.substring(0, index) : target;
        }

        public String query() {
            int index = target.indexOf('?');
            return index >= 0 ? target.substring(index + 1) : null;
        }

    }

    /**
     * 请求处理：直接写原始 HTTP 响应字节；返回后关闭连接
     */
    @FunctionalInterface
    public interface Handler {

        void handle(Request request, OutputStream out) throws Exception;

    }

    /**
     * 在 {@link Handler} 中抛出：读完请求后不写任何响应，以 TCP RST 中止连接
     */
    public static final class AbortConnection extends RuntimeException {

        public AbortConnection() {
            super(null, null, false, false);
        }

    }

    private final ServerSocket serverSocket;
    private final Handler handler;
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final List<Socket> sockets = new CopyOnWriteArrayList<>();
    private volatile boolean closed;

    public SpeechMockServer(Handler handler) throws IOException {
        this.handler = handler;
        this.serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        Thread thread = new Thread(this::acceptLoop, "speech-mock-server");
        thread.setDaemon(true);
        thread.start();
    }

    public String url() {
        return "http://127.0.0.1:" + serverSocket.getLocalPort();
    }

    public String hostAndPort() {
        return "127.0.0.1:" + serverSocket.getLocalPort();
    }

    public List<Request> requests() {
        return requests;
    }

    private void acceptLoop() {
        while (!closed) {
            try {
                Socket socket = serverSocket.accept();
                // 关闭 Nagle：分块响应逐块立即发出，避免小块与延迟确认叠加导致的等待
                socket.setTcpNoDelay(true);
                sockets.add(socket);
                Thread thread = new Thread(() -> serve(socket), "speech-mock-connection");
                thread.setDaemon(true);
                thread.start();
            } catch (IOException ex) {
                return;
            }
        }
    }

    private void serve(Socket client) {
        try (Socket socket = client) {
            Request request = readRequest(socket.getInputStream());
            if (request == null) {
                return;
            }
            requests.add(request);
            OutputStream out = socket.getOutputStream();
            try {
                handler.handle(request, out);
            } catch (AbortConnection ex) {
                // SO_LINGER=0：close 时发送 RST，而不是正常的 FIN
                socket.setSoLinger(true, 0);
                return;
            }
            out.flush();
        } catch (Exception ignored) {
            // 测试场景：客户端超时断开等
        }
    }

    private static Request readRequest(InputStream in) throws IOException {
        // 1. 请求行与请求头
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        int window = 0;
        int b;
        boolean complete = false;
        while ((b = in.read()) != -1) {
            head.write(b);
            window = (window << 8) | (b & 0xFF);
            if (window == 0x0D0A0D0A) {
                complete = true;
                break;
            }
        }
        if (!complete) {
            return null;
        }
        String[] lines = new String(head.toByteArray(), StandardCharsets.ISO_8859_1).split("\r\n");
        String[] requestLine = lines[0].split(" ");
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int index = lines[i].indexOf(':');
            if (index > 0) {
                headers.put(lines[i].substring(0, index).trim().toLowerCase(Locale.ROOT), lines[i].substring(index + 1).trim());
            }
        }
        // 2. 请求体：Content-Length，或 chunked（RestClient 流式写入 JSON 时使用）
        byte[] body = "chunked".equalsIgnoreCase(headers.get("transfer-encoding")) ? readChunkedBody(in)
                : IoUtil.readBytes(in, Integer.parseInt(headers.getOrDefault("content-length", "0")));
        return new Request(requestLine[0], requestLine[1], headers, body);
    }

    private static byte[] readChunkedBody(InputStream in) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while (true) {
            // 块大小行：十六进制，可带扩展
            String sizeLine = readLine(in);
            int size = Integer.parseInt(sizeLine.split(";")[0].trim(), 16);
            if (size == 0) {
                // 结束：跳过 trailer，直到空行
                while (!readLine(in).isEmpty()) {
                    // 忽略 trailer
                }
                return body.toByteArray();
            }
            body.write(IoUtil.readBytes(in, size));
            readLine(in); // 块后的 CRLF
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1 && b != '\n') {
            if (b != '\r') {
                line.write(b);
            }
        }
        return new String(line.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    // ========== 响应 ==========

    /**
     * 写完整响应
     *
     * @param headerPairs 额外响应头：name, value, name, value...
     */
    public static void writeResponse(OutputStream out, int status, String contentType, byte[] body,
                                     String... headerPairs) throws IOException {
        StringBuilder head = new StringBuilder("HTTP/1.1 ").append(status).append(" X\r\n");
        if (contentType != null) {
            head.append("Content-Type: ").append(contentType).append("\r\n");
        }
        appendHeaders(head, headerPairs);
        head.append("Content-Length: ").append(body.length).append("\r\n");
        head.append("Connection: close\r\n\r\n");
        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    public static void writeJson(OutputStream out, int status, String json, String... headerPairs) throws IOException {
        writeResponse(out, status, "application/json", json.getBytes(StandardCharsets.UTF_8), headerPairs);
    }

    /**
     * 写分块响应：每块单独 flush 并稍作停顿，让客户端分多次读到
     *
     * @param terminate 是否写结束块；false 时直接返回（随后关闭连接），模拟提前断开
     */
    public static void writeChunked(OutputStream out, int status, String contentType, List<byte[]> chunks,
                                    boolean terminate, String... headerPairs) throws IOException, InterruptedException {
        StringBuilder head = new StringBuilder("HTTP/1.1 ").append(status).append(" X\r\n");
        head.append("Content-Type: ").append(contentType).append("\r\n");
        appendHeaders(head, headerPairs);
        head.append("Transfer-Encoding: chunked\r\n");
        head.append("Connection: close\r\n\r\n");
        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
        for (byte[] chunk : chunks) {
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            frame.write((Integer.toHexString(chunk.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
            frame.write(chunk);
            frame.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            out.write(frame.toByteArray());
            out.flush();
            Thread.sleep(5);
        }
        if (terminate) {
            out.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
        }
    }

    private static void appendHeaders(StringBuilder head, String... headerPairs) {
        for (int i = 0; i + 1 < headerPairs.length; i += 2) {
            head.append(headerPairs[i]).append(": ").append(headerPairs[i + 1]).append("\r\n");
        }
    }

    /**
     * 按固定大小切分字节，用于模拟任意分块边界
     */
    public static List<byte[]> split(byte[] data, int size) {
        List<byte[]> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < data.length; i += size) {
            chunks.add(java.util.Arrays.copyOfRange(data, i, Math.min(data.length, i + size)));
        }
        return chunks;
    }

    /**
     * 按指定位置切分字节：用于把边界精确放在多字节字符、base64、帧的中间
     */
    public static List<byte[]> splitAt(byte[] data, int... offsets) {
        List<byte[]> chunks = new java.util.ArrayList<>();
        int start = 0;
        for (int offset : offsets) {
            chunks.add(java.util.Arrays.copyOfRange(data, start, offset));
            start = offset;
        }
        chunks.add(java.util.Arrays.copyOfRange(data, start, data.length));
        return chunks;
    }

    // ========== 音频样本 ==========

    /**
     * 最小的 mp3 样本：MPEG 帧同步头 + 填充
     */
    public static byte[] mp3(int length) {
        byte[] data = new byte[Math.max(length, 4)];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xFB;
        data[2] = (byte) 0x90;
        for (int i = 3; i < data.length; i++) {
            data[i] = (byte) (i * 31);
        }
        return data;
    }

    /**
     * 最小的 wav 样本：RIFF....WAVE 头 + 填充
     */
    public static byte[] wav(int length) {
        byte[] data = new byte[Math.max(length, 12)];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, data, 0, 4);
        System.arraycopy("WAVE".getBytes(StandardCharsets.US_ASCII), 0, data, 8, 4);
        for (int i = 12; i < data.length; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        serverSocket.close();
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

}
