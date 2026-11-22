package cn.iocoder.yudao.framework.sse.core.codec;

import cn.hutool.core.util.StrUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * SSE 响应头，避免代理（例如说 Nginx）缓冲或压缩事件流
 *
 * 1. Content-Type: text/event-stream;charset=UTF-8
 * 2. Cache-Control: no-cache, no-transform
 * 3. X-Accel-Buffering: no
 * 4. Connection: keep-alive，仅 HTTP/1.x 设置，HTTP/2 禁止设置
 *
 * @author 芋道源码
 */
public class SseResponseHeaderSupport {

    public static final MediaType TEXT_EVENT_STREAM_UTF8 = new MediaType(MediaType.TEXT_EVENT_STREAM, StandardCharsets.UTF_8);

    private static final String HEADER_X_ACCEL_BUFFERING = "X-Accel-Buffering";

    /**
     * 构建 SSE 响应头
     *
     * @param request 请求
     * @return 响应头
     */
    public static HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(TEXT_EVENT_STREAM_UTF8);
        headers.setCacheControl("no-cache, no-transform");
        headers.set(HEADER_X_ACCEL_BUFFERING, "no");
        if (StrUtil.startWithIgnoreCase(request.getProtocol(), "HTTP/1")) {
            headers.setConnection("keep-alive");
        }
        return headers;
    }

}
