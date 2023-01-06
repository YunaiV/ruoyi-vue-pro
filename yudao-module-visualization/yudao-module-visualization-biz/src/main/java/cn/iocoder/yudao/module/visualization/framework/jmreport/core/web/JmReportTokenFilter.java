package cn.iocoder.yudao.module.visualization.framework.jmreport.core.web;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import cn.iocoder.yudao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import lombok.RequiredArgsConstructor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 积木报表 token 处理，将积木报表请求头中的 token 转换成 spring security 的 auth head
 */
@RequiredArgsConstructor
public class JmReportTokenFilter implements Filter {
    /**
     * 积木 token 请求头
     */
    private static final String JM_TOKEN_HEADER = "X-Access-Token";
    /**
     * 系统内置请求头
     */
    private static final String TOKEN_HEADER = "Authorization";
    /**
     * auth 相关格式
     */
    private static final String AUTHORIZATION_FORMAT = "Bearer %s";

    private final OAuth2TokenApi oauth2TokenApi;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 积木请求头
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String token = req.getHeader(JM_TOKEN_HEADER);
        if (StrUtil.isNotEmpty(token)) {
            // 1. 增加请求头
            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
            requestWrapper.addHeader(TOKEN_HEADER, String.format(AUTHORIZATION_FORMAT, token));

            OAuth2AccessTokenCheckRespDTO resp = oauth2TokenApi.checkAccessToken(token);
            Optional<LoginUser> optUser = Optional.ofNullable(resp)
                    .map(
                            t -> new LoginUser().setId(t.getUserId())
                                    .setUserType(t.getUserType())
                                    .setTenantId(t.getTenantId())
                                    .setScopes(t.getScopes())
                    );
            if (optUser.isPresent()) {
                // 2. 设置登录用户类型
                WebFrameworkUtils.setLoginUserType(servletRequest, optUser.get().getUserType());
                filterChain.doFilter(requestWrapper, servletResponse);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * request 包装类，用于修改 head
     *
     * <a href="https://stackoverflow.com/questions/2811769/adding-an-http-header-to-the-request-in-a-servlet-filter">add request head</a>
     */
    public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        /**
         * construct a wrapper for this request
         *
         * @param request
         */
        public HeaderMapRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        private Map<String, String> headerMap = new HashMap<String, String>();

        /**
         * add a header with given name and value
         *
         * @param name
         * @param value
         */
        public void addHeader(String name, String value) {
            headerMap.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = super.getHeader(name);
            if (headerMap.containsKey(name)) {
                headerValue = headerMap.get(name);
            }
            return headerValue;
        }

        /**
         * get the Header names
         */
        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            for (String name : headerMap.keySet()) {
                names.add(name);
            }
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = Collections.list(super.getHeaders(name));
            if (headerMap.containsKey(name)) {
                values.add(headerMap.get(name));
            }
            return Collections.enumeration(values);
        }

    }

}
