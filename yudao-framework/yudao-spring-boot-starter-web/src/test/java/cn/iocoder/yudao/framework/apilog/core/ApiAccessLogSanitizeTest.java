package cn.iocoder.yudao.framework.apilog.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.filter.ApiAccessLogFilter;
import cn.iocoder.yudao.framework.apilog.core.interceptor.ApiAccessLogInterceptor;
import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * API 访问日志脱敏测试：访问日志入库、非 prod 控制台打印、异常日志入库三个出口使用相同规则
 *
 * @author 芋道源码
 */
class ApiAccessLogSanitizeTest {

    private static final String BODY = "{\"username\":\"fake-user\",\"password\":\"FAKE_PASSWORD\","
            + "\"config\":{\"accessKey\":\"FAKE_ACCESS_KEY\",\"endpoint\":\"https://s3.example.com\","
            + "\"items\":[{\"token\":\"FAKE_TOKEN\",\"accessSecret\":\"FAKE_ACCESS_SECRET\",\"name\":\"keep\"}]},"
            + "\"customKey\":\"FAKE_CUSTOM_KEY\"}";

    private GenericApplicationContext context;
    private ListAppender<ILoggingEvent> appender;
    private Logger interceptorLogger;

    @BeforeEach
    void setUp() {
        new WebFrameworkUtils(new WebProperties());
        // 非 prod 环境，拦截器才会打印请求参数
        context = new GenericApplicationContext();
        context.getEnvironment().setActiveProfiles("dev");
        context.refresh();
        new SpringUtil().setApplicationContext(context);
        // 收集拦截器打印的日志
        interceptorLogger = (Logger) LoggerFactory.getLogger(ApiAccessLogInterceptor.class);
        appender = new ListAppender<>();
        appender.start();
        interceptorLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        interceptorLogger.detachAppender(appender);
        context.close();
    }

    @Test
    void testSanitizeJson_defaultAndAnnotationKeys() {
        String result = ApiAccessLogFilter.sanitizeJson(BODY, new String[]{"customKey"});

        // 默认敏感字段和注解声明字段在嵌套对象、数组中都被移除
        assertNotNull(result);
        assertFalse(result.contains("FAKE_PASSWORD"));
        assertFalse(result.contains("FAKE_TOKEN"));
        assertFalse(result.contains("FAKE_ACCESS_KEY"));
        assertFalse(result.contains("FAKE_ACCESS_SECRET"));
        assertFalse(result.contains("FAKE_CUSTOM_KEY"));
        // 普通字段保留
        assertTrue(result.contains("fake-user"));
        assertTrue(result.contains("https://s3.example.com"));
        assertTrue(result.contains("keep"));
    }

    @Test
    void testSanitizeJson_withoutAnnotationKeys() {
        String result = ApiAccessLogFilter.sanitizeJson(BODY, null);

        // 未注解时只移除默认敏感字段
        assertFalse(result.contains("FAKE_PASSWORD"));
        assertFalse(result.contains("FAKE_TOKEN"));
        assertFalse(result.contains("FAKE_ACCESS_KEY"));
        assertTrue(result.contains("FAKE_CUSTOM_KEY"));
    }

    @Test
    void testSanitizeJson_realCredentialFields() {
        // 修改密码、短信渠道、OAuth2 客户端、社交客户端、文件存储的真实字段，未注解也需要移除
        String body = "{\"oldPassword\":\"FAKE_OLD_PASSWORD\",\"newPassword\":\"FAKE_NEW_PASSWORD\","
                + "\"apiKey\":\"FAKE_API_KEY\",\"apiSecret\":\"FAKE_API_SECRET\",\"secret\":\"FAKE_SECRET\","
                + "\"clientSecret\":\"FAKE_CLIENT_SECRET\",\"config\":{\"accessKey\":\"FAKE_ACCESS_KEY\","
                + "\"accessSecret\":\"FAKE_ACCESS_SECRET\",\"password\":\"FAKE_FTP_PASSWORD\","
                + "\"privateKey\":\"FAKE_PRIVATE_KEY\",\"privateKeyPassphrase\":\"FAKE_PASSPHRASE\"},"
                + "\"name\":\"keep\"}";

        String result = ApiAccessLogFilter.sanitizeJson(body, null);

        assertTrue(result.contains("keep"));
        for (String value : new String[]{"FAKE_OLD_PASSWORD", "FAKE_NEW_PASSWORD", "FAKE_API_KEY", "FAKE_API_SECRET",
                "FAKE_SECRET", "FAKE_CLIENT_SECRET", "FAKE_ACCESS_KEY", "FAKE_ACCESS_SECRET", "FAKE_FTP_PASSWORD",
                "FAKE_PRIVATE_KEY", "FAKE_PASSPHRASE"}) {
            assertFalse(result.contains(value), value);
        }
    }

    @Test
    void testSanitizeJson_malformed() {
        Logger filterLogger = (Logger) LoggerFactory.getLogger(ApiAccessLogFilter.class);
        ListAppender<ILoggingEvent> filterAppender = new ListAppender<>();
        filterAppender.start();
        filterLogger.addAppender(filterAppender);
        try {
            // 格式错误的请求体不回退原文
            assertNull(ApiAccessLogFilter.sanitizeJson("{\"password\":\"FAKE_PASSWORD\",", null));
            assertNull(ApiAccessLogFilter.sanitizeJson("password=FAKE_PASSWORD", null));
            // 也不打印原文
            assertFalse(joinMessages(filterAppender).contains("FAKE_PASSWORD"));
        } finally {
            filterLogger.detachAppender(filterAppender);
        }
    }

    @Test
    void testSanitizeMap() {
        Map<String, String> query = new HashMap<>();
        query.put("username", "fake-user");
        query.put("password", "FAKE_PASSWORD");
        query.put("client_secret", "FAKE_CLIENT_SECRET");
        query.put("customKey", "FAKE_CUSTOM_KEY");

        String result = ApiAccessLogFilter.sanitizeMap(query, new String[]{"customKey"});

        assertFalse(result.contains("FAKE_PASSWORD"));
        assertFalse(result.contains("FAKE_CLIENT_SECRET"));
        assertFalse(result.contains("FAKE_CUSTOM_KEY"));
        assertTrue(result.contains("fake-user"));
        assertNull(ApiAccessLogFilter.sanitizeMap(new HashMap<>(), null));
    }

    @Test
    void testInterceptor_jsonBodyWithAnnotation() throws NoSuchMethodException {
        MockHttpServletRequest request = jsonRequest(BODY);

        new ApiAccessLogInterceptor().preHandle(request, new MockHttpServletResponse(), sanitizedHandler());

        // 控制台打印与访问日志入库使用相同的脱敏规则
        String messages = joinMessages(appender);
        assertTrue(messages.contains("fake-user"));
        assertFalse(messages.contains("FAKE_PASSWORD"));
        assertFalse(messages.contains("FAKE_TOKEN"));
        assertFalse(messages.contains("FAKE_ACCESS_KEY"));
        assertFalse(messages.contains("FAKE_ACCESS_SECRET"));
    }

    @Test
    void testInterceptor_jsonBodyWithoutHandler() {
        MockHttpServletRequest request = jsonRequest("{\"username\":\"fake-user\",\"password\":\"FAKE_PASSWORD\"}");

        new ApiAccessLogInterceptor().preHandle(request, new MockHttpServletResponse(), null);

        // 没有处理方法时，仍移除默认敏感字段
        String messages = joinMessages(appender);
        assertTrue(messages.contains("fake-user"));
        assertFalse(messages.contains("FAKE_PASSWORD"));
    }

    @Test
    void testInterceptor_queryParams() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin-api/system/auth/login");
        request.addParameter("username", "fake-user");
        request.addParameter("password", "FAKE_PASSWORD");
        request.addParameter("customKey", "FAKE_CUSTOM_KEY");

        new ApiAccessLogInterceptor().preHandle(request, new MockHttpServletResponse(), sanitizedHandler());

        String messages = joinMessages(appender);
        assertTrue(messages.contains("fake-user"));
        assertFalse(messages.contains("FAKE_PASSWORD"));
        assertFalse(messages.contains("FAKE_CUSTOM_KEY"));
    }

    @Test
    void testInterceptor_oauth2ClientSecretParam() {
        // OAuth2 开放接口通过 client_secret、refresh_token 表单参数传递客户端密钥、刷新令牌
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin-api/system/oauth2/token");
        request.addParameter("client_id", "fake-client");
        request.addParameter("client_secret", "FAKE_CLIENT_SECRET");
        request.addParameter("grant_type", "refresh_token");
        request.addParameter("refresh_token", "FAKE_REFRESH_TOKEN");

        new ApiAccessLogInterceptor().preHandle(request, new MockHttpServletResponse(), null);

        String messages = joinMessages(appender);
        assertTrue(messages.contains("fake-client"));
        assertFalse(messages.contains("FAKE_CLIENT_SECRET"));
        assertFalse(messages.contains("FAKE_REFRESH_TOKEN"));
    }

    @Test
    void testInterceptor_malformedJson() {
        MockHttpServletRequest request = jsonRequest("{\"password\":\"FAKE_PASSWORD\"");

        new ApiAccessLogInterceptor().preHandle(request, new MockHttpServletResponse(), null);

        assertFalse(joinMessages(appender).contains("FAKE_PASSWORD"));
    }

    @Test
    void testGlobalExceptionHandler_sanitizeRequestParams() throws NoSuchMethodException {
        ApiErrorLogCommonApi apiErrorLogApi = mock(ApiErrorLogCommonApi.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler("test", apiErrorLogApi);
        MockHttpServletRequest request = jsonRequest(BODY);
        request.addParameter("refreshToken", "FAKE_REFRESH_TOKEN");
        request.addParameter("client_secret", "FAKE_CLIENT_SECRET");
        request.addParameter("customKey", "FAKE_CUSTOM_KEY");
        request.addParameter("page", "1");
        request.setAttribute(ApiAccessLogInterceptor.ATTRIBUTE_HANDLER_METHOD, sanitizedHandler());

        handler.defaultExceptionHandler(request, new RuntimeException("fake failure"));

        // 异常日志的请求参数被脱敏，异常堆栈保留
        ArgumentCaptor<ApiErrorLogCreateReqDTO> captor = ArgumentCaptor.forClass(ApiErrorLogCreateReqDTO.class);
        verify(apiErrorLogApi).createApiErrorLogAsync(captor.capture());
        ApiErrorLogCreateReqDTO errorLog = captor.getValue();
        assertTrue(errorLog.getRequestParams().contains("fake-user"));
        assertTrue(errorLog.getRequestParams().contains("page"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_PASSWORD"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_TOKEN"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_REFRESH_TOKEN"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_ACCESS_KEY"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_ACCESS_SECRET"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_CLIENT_SECRET"));
        assertFalse(errorLog.getRequestParams().contains("FAKE_CUSTOM_KEY"));
        assertTrue(errorLog.getExceptionStackTrace().contains("fake failure"));
        // query 保持 JSON 对象形状，不是被二次序列化的字符串
        JsonNode requestParams = JsonUtils.parseTree(errorLog.getRequestParams());
        assertTrue(requestParams.get("query").isObject());
        assertEquals("1", requestParams.get("query").get("page").asText());
        assertFalse(requestParams.get("query").has("refreshToken"));
        assertFalse(requestParams.get("query").has("client_secret"));
    }

    @Test
    void testGlobalExceptionHandler_malformedJsonWithoutHandler() {
        ApiErrorLogCommonApi apiErrorLogApi = mock(ApiErrorLogCommonApi.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler("test", apiErrorLogApi);
        MockHttpServletRequest request = jsonRequest("{\"password\":\"FAKE_PASSWORD\"");

        handler.defaultExceptionHandler(request, new RuntimeException("fake failure"));

        ArgumentCaptor<ApiErrorLogCreateReqDTO> captor = ArgumentCaptor.forClass(ApiErrorLogCreateReqDTO.class);
        verify(apiErrorLogApi).createApiErrorLogAsync(captor.capture());
        assertFalse(captor.getValue().getRequestParams().contains("FAKE_PASSWORD"));
    }

    private static MockHttpServletRequest jsonRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/admin-api/test/update");
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static HandlerMethod sanitizedHandler() throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getMethod("update"));
    }

    private static String joinMessages(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
    }

    static class TestController {

        @ApiAccessLog(sanitizeKeys = {"customKey"})
        public void update() {
        }

    }

}
