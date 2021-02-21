package cn.iocoder.dashboard.framework.web.core.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HTMLFilter;
import org.springframework.http.MediaType;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Xss 请求 Wrapper
 *
 * @author 芋道源码
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 基于线程级别的 HTMLFilter 对象，因为它线程非安全
     */
    private static final ThreadLocal<HTMLFilter> HTML_FILTER = ThreadLocal.withInitial(() -> {
        HTMLFilter htmlFilter = new HTMLFilter();
        // 反射修改 encodeQuotes 属性为 false，避免 " 被转移成 &quot; 字符
        ReflectUtil.setFieldValue(htmlFilter, "encodeQuotes", false);
        return htmlFilter;
    });

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    private static String filterHtml(String content) {
        if (StrUtil.isEmpty(content)) {
            return content;
        }
        return HTML_FILTER.get().filter(content);
    }

    // ========== IO 流相关 ==========

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果非 json 请求，不进行 Xss 处理
        if (!StrUtil.startWithIgnoreCase(super.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            return super.getInputStream();
        }

        // 读取内容，并过滤
        String content = IoUtil.readUtf8(super.getInputStream());
        content = filterHtml(content);
        final ByteArrayInputStream newInputStream = new ByteArrayInputStream(content.getBytes());
        // 返回 ServletInputStream
        return new ServletInputStream() {

            @Override
            public int read() {
                return newInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {}

        };
    }

    // ========== Param 相关 ==========

    // ========== Header 相关 ==========

}
