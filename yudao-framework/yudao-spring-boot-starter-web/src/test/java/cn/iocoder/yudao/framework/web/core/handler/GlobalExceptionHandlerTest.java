package cn.iocoder.yudao.framework.web.core.handler;

import cn.iocoder.yudao.framework.apilog.core.service.ApiErrorLogFrameworkService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private String applicationName = "tudou";
    @Mock
    private ApiErrorLogFrameworkService apiErrorLogFrameworkService;

    @Test
    public void testAllExceptionHandler_MissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("test", "echo");
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数缺失:test", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_MethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                null, String.class, null, null, null);
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数类型错误:Failed to convert value of type 'null' to required type 'java.lang.String'",
                result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_BindException() {
        // 模拟请求参数
        BindException ex = mock(BindException.class);
        // 模拟 FieldError
        FieldError fieldError = new FieldError("test", "field", "message");
        when(ex.getFieldError()).thenReturn(fieldError);

        // 执行
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数不正确:message", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_ConstraintViolationException() {
        // 模拟请求参数
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        // mock ConstraintViolation
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(ex.getConstraintViolations()).thenReturn(Collections.singleton(violation));
        when(violation.getMessage()).thenReturn("message");

        // 执行
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数不正确:message", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_ValidationException() {
        // 模拟请求参数
        ValidationException ex = mock(ValidationException.class);

        // 执行
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数不正确", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_NoHandlerFoundException() {
        NoHandlerFoundException ex = new NoHandlerFoundException("get", "/test", null);
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(NOT_FOUND.getCode(), result.getCode());
        assertEquals("请求地址不存在:/test", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_HttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("get", "message");
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(METHOD_NOT_ALLOWED.getCode(), result.getCode());
        assertEquals("请求方法不正确:message", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_RequestNotPermitted() {
        RequestNotPermitted ex = mock(RequestNotPermitted.class);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // 调用
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(req, ex);
        assertEquals(TOO_MANY_REQUESTS.getCode(), result.getCode());
        assertEquals("请求过于频繁，请稍后重试", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_AccessDeniedException() {
        AccessDeniedException ex = mock(AccessDeniedException.class);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // 调用
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(req, ex);
        assertEquals(FORBIDDEN.getCode(), result.getCode());
        assertEquals("没有该操作权限", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_ServiceException() {
        ServiceException ex = new ServiceException(1, "test");

        // 调用
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(null, ex);
        assertEquals(1, result.getCode());
        assertEquals("test", result.getMsg());
    }

    @Test
    public void testAllExceptionHandler_Throwable() {
        Throwable ex = new Throwable("test");
        HttpServletRequest req = mock(HttpServletRequest.class);

        // 调用
        CommonResult<?> result = globalExceptionHandler.allExceptionHandler(req, ex);
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.getCode());
        assertEquals("系统异常", result.getMsg());
    }

}
