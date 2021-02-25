package cn.iocoder.dashboard.framework.logger.apilog.core.filter;

import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.dashboard.framework.web.config.WebProperties;
import cn.iocoder.dashboard.util.servlet.ServletUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * API 访问日志 Filter
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Slf4j
public class ApiAccessLogFilter extends OncePerRequestFilter {

    private final WebProperties webProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(webProperties.getApiPrefix());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获得开始时间
        Date startTime = new Date();
        // 提前获得参数，避免 XssFilter 过滤处理
        Map<String, String> queryString = ServletUtil.getParamMap(request);
        String requestBody = ServletUtils.isJsonRequest(request) ? ServletUtil.getBody(request) : null;

        try {
            // 继续过滤器
            filterChain.doFilter(request, response);
            // 正常执行，记录日志
            createApiAccessLog(request, startTime, queryString, requestBody, null);
        } catch (Exception ex) {
            // 异常执行，记录日志
            createApiAccessLog(request, startTime, queryString, requestBody, ex);
            throw ex;
        }
    }

    private void createApiAccessLog(HttpServletRequest request, Date startTime,
                                    Map<String, String> queryString, String requestBody, Exception ex) {
        try {
            ApiAccessLogCreateDTO createDTO = this.buildApiAccessLogDTO(request, startTime, queryString, requestBody, ex);

        } catch (Exception e) {
            log.error("[createApiAccessLog][url({}) 发生异常]", request.getRequestURI(), ex);
        }
    }

    private ApiAccessLogCreateDTO buildApiAccessLogDTO(HttpServletRequest request, Date startTime,
                                                       Map<String, String> queryString, String requestBody, Exception ex) {
        return null;
    }

}
