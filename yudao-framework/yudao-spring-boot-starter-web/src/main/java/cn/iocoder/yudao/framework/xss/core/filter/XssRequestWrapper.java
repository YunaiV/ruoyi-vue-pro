package cn.iocoder.yudao.framework.xss.core.filter;

import cn.iocoder.yudao.framework.xss.core.clean.XssCleaner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Xss 请求 Wrapper
 *
 * @author 芋道源码
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final XssCleaner xssCleaner;

    public XssRequestWrapper(HttpServletRequest request, XssCleaner xssCleaner) {
        super(request);
        this.xssCleaner = xssCleaner;
    }

    // ============================ parameter ============================
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        Map<String, String[]> parameters = super.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String[] values = entry.getValue();
            for (int i = 0; i < values.length; i++) {
                values[i] = xssCleaner.clean(values[i]);
            }
            map.put(entry.getKey(), values);
        }
        return map;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = xssCleaner.clean(values[i]);
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        return xssCleaner.clean(value);
    }

    // ============================ attribute ============================
    @Override
    public Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        if (value instanceof String) {
            return xssCleaner.clean((String) value);
        }
        return value;
    }

    // ============================ header ============================
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null) {
            return null;
        }
        return xssCleaner.clean(value);
    }

    // ============================ queryString ============================
    @Override
    public String getQueryString() {
        String value = super.getQueryString();
        if (value == null) {
            return null;
        }
        return xssCleaner.clean(value);
    }

}
