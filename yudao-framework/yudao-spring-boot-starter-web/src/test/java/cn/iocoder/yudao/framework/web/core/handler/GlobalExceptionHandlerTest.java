package cn.iocoder.yudao.framework.web.core.handler;

import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * {@link GlobalExceptionHandler} 的单元测试
 *
 * 重点验证“表不存在”的识别：按 SQLState / 错误码识别多种数据库，其它 SQL 异常不会被当作表不存在；
 * 以及请求体读取失败时，客户端参数结构错误返回 BAD_REQUEST，其它错误仍按系统异常处理
 *
 * @author 芋道源码
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler("test", mock(ApiErrorLogCommonApi.class));
        request = mock(HttpServletRequest.class);
    }

    @Test
    public void testDefaultExceptionHandler_tableNotExists_mysql() {
        // 准备参数
        Exception ex = new RuntimeException("nested exception",
                new SQLException("Table 'ruoyi-vue-pro.erp_product' doesn't exist", "42S02", 1146));

        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);
        // 断言
        assertEquals(NOT_IMPLEMENTED.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("yudao-module-erp"));
    }

    @Test
    public void testDefaultExceptionHandler_tableNotExists_postgresql() {
        // 准备参数：PostgreSQL 的文案不包含 doesn't exist，通过 SQLState 42P01 识别
        Exception ex = new RuntimeException("nested exception",
                new SQLException("ERROR: relation \"bpm_category\" does not exist", "42P01"));

        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);
        // 断言
        assertEquals(NOT_IMPLEMENTED.getCode(), result.getCode());
        assertTrue(result.getMsg().contains("yudao-module-bpm"));
    }

    @Test
    public void testDefaultExceptionHandler_tableNotExists_noPrefixMatched() {
        // 准备参数：Oracle 的文案不包含表名，无法匹配模块前缀，按系统异常处理
        Exception ex = new RuntimeException("nested exception",
                new SQLException("ORA-00942: table or view does not exist\n", "42000", 942));

        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);
        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
    }

    @Test
    public void testDefaultExceptionHandler_columnNotExists() {
        // 准备参数：列不存在，即使文案包含模块前缀，也不是表不存在
        Exception ex = new RuntimeException("nested exception",
                new SQLException("ERROR: column \"xxx\" of relation \"bpm_category\" does not exist", "42703"));

        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);
        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
    }

    @Test
    public void testDefaultExceptionHandler_permissionDenied() {
        // 准备参数：权限不足，不是表不存在
        Exception ex = new RuntimeException("nested exception",
                new SQLException("SELECT command denied to user 'u'@'localhost' for table 'erp_product'", "42000", 1142));

        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);
        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
    }

    @Test
    public void testDefaultExceptionHandler_permissionDeniedWithLegacyPhrase() {
        Exception ex = new RuntimeException("nested exception", new SQLException(
                "SELECT command denied to user 'doesn't exist'@'localhost' for table 'erp_product'",
                "42000", 1142));

        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request, ex);

        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
    }

    @Test
    public void testDefaultExceptionHandler_nullMessage() {
        // 调用
        CommonResult<?> result = globalExceptionHandler.defaultExceptionHandler(request,
                new RuntimeException((String) null));
        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
    }

    // ========== HttpMessageNotReadableException ==========

    @Test
    public void testHttpMessageNotReadable_mismatchedInput() {
        // 准备参数：Map 字段传了数组，Jackson 抛出 MismatchedInputException
        HttpMessageNotReadableException ex = buildNotReadableException(
                "{\"levels\":[{\"options\":[]}]}", new TypeReference<Map<String, List<Map<String, Map<String, Object>>>>>() {});
        assertInstanceOf(MismatchedInputException.class, ex.getCause());

        // 调用
        CommonResult<?> result = globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(ex);
        // 断言
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数类型错误:levels[0].options", result.getMsg());
    }

    @Test
    public void testHttpMessageNotReadable_mismatchedInputRoot() {
        // 准备参数：请求体本身应该是对象，结果传了数组
        HttpMessageNotReadableException ex = buildNotReadableException("[]",
                new TypeReference<Map<String, Object>>() {});

        // 调用
        CommonResult<?> result = globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(ex);
        // 断言
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数类型错误", result.getMsg());
    }

    @Test
    public void testHttpMessageNotReadable_invalidFormat() {
        // 准备参数：Integer 字段传了字符串，InvalidFormatException 仍按原逻辑返回值
        HttpMessageNotReadableException ex = buildNotReadableException("{\"sort\":\"abc\"}",
                new TypeReference<Map<String, Integer>>() {});
        assertInstanceOf(InvalidFormatException.class, ex.getCause());

        // 调用
        CommonResult<?> result = globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(ex);
        // 断言
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数类型错误:abc", result.getMsg());
    }

    @Test
    public void testHttpMessageNotReadable_bodyMissing() {
        // 准备参数
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Required request body is missing: public void test()", mock(HttpInputMessage.class));

        // 调用
        CommonResult<?> result = globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(ex);
        // 断言
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数类型错误: request body 缺失", result.getMsg());
    }

    @Test
    public void testHttpMessageNotReadable_otherErrors() {
        // 准备参数：JSON 语法错误、无法构造的类型（服务端定义问题），都不属于 MismatchedInputException
        HttpMessageNotReadableException parseError = buildNotReadableException("{\"a\":}",
                new TypeReference<Map<String, Object>>() {});
        HttpMessageNotReadableException definitionError = buildNotReadableException("{}",
                new TypeReference<Runnable>() {});
        assertInstanceOf(JsonParseException.class, parseError.getCause());
        assertInstanceOf(InvalidDefinitionException.class, definitionError.getCause());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            // 调用，并断言：仍按系统异常处理
            assertEquals(INTERNAL_SERVER_ERROR.getCode(),
                    globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(parseError).getCode());
            assertEquals(INTERNAL_SERVER_ERROR.getCode(),
                    globalExceptionHandler.methodArgumentTypeInvalidFormatExceptionHandler(definitionError).getCode());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * 用真实的 Jackson 解析异常，构造 Spring 读取请求体失败的 HttpMessageNotReadableException
     */
    private static HttpMessageNotReadableException buildNotReadableException(String json, TypeReference<?> type) {
        JsonProcessingException cause = assertThrows(JsonProcessingException.class,
                () -> new ObjectMapper().readValue(json, type));
        return new HttpMessageNotReadableException("JSON parse error: " + cause.getOriginalMessage(), cause,
                mock(HttpInputMessage.class));
    }

}
