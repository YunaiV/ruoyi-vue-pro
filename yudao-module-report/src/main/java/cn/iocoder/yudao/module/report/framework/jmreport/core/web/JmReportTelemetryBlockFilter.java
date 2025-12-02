package cn.iocoder.yudao.module.report.framework.jmreport.core.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 积木报表遥测阻止过滤器
 * <p>
 * 拦截积木报表的 HTML 响应，注入脚本以阻止百度统计（hm.baidu.com）的加载
 * 这是由于积木报表内嵌了百度统计代码，无法通过配置禁用
 *
 * @author yudao
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JmReportTelemetryBlockFilter implements Filter {

    /**
     * 需要注入的脚本 - 阻止百度统计加载
     */
    private static final String TELEMETRY_BLOCK_SCRIPT = """
        <script>
        (function() {
            // 阻止百度统计 (hm.baidu.com)
            var originalWrite = document.write;
            document.write = function(content) {
                if (typeof content === 'string' && content.includes('hm.baidu.com')) {
                    console.log('[Telemetry Blocked] document.write: hm.baidu.com');
                    return;
                }
                return originalWrite.apply(document, arguments);
            };

            var originalFetch = window.fetch;
            window.fetch = function(url, options) {
                if (typeof url === 'string' && url.includes('hm.baidu.com')) {
                    console.log('[Telemetry Blocked] fetch:', url);
                    return Promise.resolve(new Response('', { status: 200 }));
                }
                return originalFetch.apply(this, arguments);
            };

            var originalXHROpen = XMLHttpRequest.prototype.open;
            XMLHttpRequest.prototype.open = function(method, url) {
                if (typeof url === 'string' && url.includes('hm.baidu.com')) {
                    console.log('[Telemetry Blocked] XHR:', url);
                    this._blockedTelemetry = true;
                }
                return originalXHROpen.apply(this, arguments);
            };

            var originalXHRSend = XMLHttpRequest.prototype.send;
            XMLHttpRequest.prototype.send = function() {
                if (this._blockedTelemetry) {
                    return;
                }
                return originalXHRSend.apply(this, arguments);
            };

            // 阻止通过 script 标签动态加载
            var originalCreateElement = document.createElement;
            document.createElement = function(tagName) {
                var element = originalCreateElement.apply(document, arguments);
                if (tagName.toLowerCase() === 'script') {
                    var originalSetAttribute = element.setAttribute;
                    element.setAttribute = function(name, value) {
                        if (name === 'src' && typeof value === 'string' && value.includes('hm.baidu.com')) {
                            console.log('[Telemetry Blocked] script src:', value);
                            return;
                        }
                        return originalSetAttribute.apply(this, arguments);
                    };
                    Object.defineProperty(element, 'src', {
                        set: function(value) {
                            if (typeof value === 'string' && value.includes('hm.baidu.com')) {
                                console.log('[Telemetry Blocked] script.src:', value);
                                return;
                            }
                            element.setAttribute('src', value);
                        },
                        get: function() {
                            return element.getAttribute('src');
                        }
                    });
                }
                return element;
            };

            console.log('[Privacy] JimuReport telemetry (hm.baidu.com) has been blocked');
        })();
        </script>
        """;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 只处理积木报表的请求
        if (!requestURI.startsWith("/jmreport")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        ContentCaptureResponseWrapper responseWrapper = new ContentCaptureResponseWrapper(httpResponse);

        chain.doFilter(request, responseWrapper);

        // 只处理 HTML 响应
        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.contains("text/html")) {
            String content = responseWrapper.getCapturedContent();
            // 在 <head> 标签后注入脚本
            String modifiedContent = content.replaceFirst("(<head[^>]*>)", "$1" + TELEMETRY_BLOCK_SCRIPT);

            byte[] modifiedBytes = modifiedContent.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(modifiedBytes.length);
            response.getOutputStream().write(modifiedBytes);
        } else {
            // 非 HTML 响应，直接输出原始内容
            responseWrapper.writeCapturedContentTo(response.getOutputStream());
        }
    }

    /**
     * 响应包装器，用于捕获响应内容
     */
    private static class ContentCaptureResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream capture;
        private ServletOutputStream output;
        private PrintWriter writer;

        public ContentCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
            this.capture = new ByteArrayOutputStream(response.getBufferSize());
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }
            if (output == null) {
                output = new ServletOutputStream() {
                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                    }

                    @Override
                    public void write(int b) {
                        capture.write(b);
                    }
                };
            }
            return output;
        }

        @Override
        public PrintWriter getWriter() {
            if (output != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(capture, StandardCharsets.UTF_8));
            }
            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (writer != null) {
                writer.flush();
            } else if (output != null) {
                output.flush();
            }
        }

        public String getCapturedContent() throws IOException {
            flushBuffer();
            return capture.toString(StandardCharsets.UTF_8);
        }

        public void writeCapturedContentTo(OutputStream os) throws IOException {
            flushBuffer();
            capture.writeTo(os);
        }
    }
}
