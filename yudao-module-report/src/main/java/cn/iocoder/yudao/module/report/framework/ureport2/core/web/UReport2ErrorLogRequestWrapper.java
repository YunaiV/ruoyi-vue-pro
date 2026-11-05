package cn.iocoder.yudao.module.report.framework.ureport2.core.web;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * 记录 UReport2 异常日志用的 {@link HttpServletRequest} 包装类
 *
 * GlobalExceptionHandler 记录异常日志时，会读取全部请求参数、JSON 请求体。而 UReport2 的请求参数包含 token（刷新令牌）、
 * tenantId、_u，保存模板时还包含模板内容（可能有数据源的账号密码），都不能写入日志。
 *
 * 因此，本包装类不对外提供请求参数、query string、请求体、上传文件；URI、请求方法、请求头、请求属性（登录用户等）仍委托给原请求，用于定位问题。
 * 本包装类不会读取原请求的参数和请求体，异常处理后，原请求仍可正常读取
 *
 * @author 芋道源码
 */
public class UReport2ErrorLogRequestWrapper extends HttpServletRequestWrapper {

    public UReport2ErrorLogRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.emptyMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String[] getParameterValues(String name) {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return -1;
            }

        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new StringReader(""));
    }

    @Override
    public Collection<Part> getParts() {
        return Collections.emptyList();
    }

    @Override
    public Part getPart(String name) {
        return null;
    }

}
