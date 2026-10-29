package cn.iocoder.yudao.framework.web.core.handler;

import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * {@link GlobalExceptionHandler} 的单元测试
 *
 * 重点验证“表不存在”的识别：按 SQLState / 错误码识别多种数据库，其它 SQL 异常不会被当作表不存在
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

}
