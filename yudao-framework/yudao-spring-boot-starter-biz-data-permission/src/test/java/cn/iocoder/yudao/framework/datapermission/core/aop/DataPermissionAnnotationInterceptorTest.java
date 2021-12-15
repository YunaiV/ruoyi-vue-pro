package cn.iocoder.yudao.framework.datapermission.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link DataPermissionAnnotationInterceptor} 的单元测试
 *
 * @author 芋道源码
 */
public class DataPermissionAnnotationInterceptorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DataPermissionAnnotationInterceptor interceptor;

    @Mock
    private MethodInvocation methodInvocation;

    @BeforeEach
    public void setUp() {
        interceptor.getDataPermissionCache().clear();
    }

    @Test // 无 @DataPermission 注解
    public void testInvoke_none() throws Throwable {
        // 参数
        mockMethodInvocation(TestNone.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("none", result);
        assertEquals(1, interceptor.getDataPermissionCache().size());
        assertTrue(CollUtil.getFirst(interceptor.getDataPermissionCache().values()).enable());
    }

    @Test // 在 Method 上有 @DataPermission 注解
    public void testInvoke_method() throws Throwable {
        // 参数
        mockMethodInvocation(TestMethod.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("method", result);
        assertEquals(1, interceptor.getDataPermissionCache().size());
        assertFalse(CollUtil.getFirst(interceptor.getDataPermissionCache().values()).enable());
    }

    @Test // 在 Class 上有 @DataPermission 注解
    public void testInvoke_class() throws Throwable {
        // 参数
        mockMethodInvocation(TestClass.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("class", result);
        assertEquals(1, interceptor.getDataPermissionCache().size());
        assertFalse(CollUtil.getFirst(interceptor.getDataPermissionCache().values()).enable());
    }

    private void mockMethodInvocation(Class<?> clazz) throws Throwable {
        Object targetObject = clazz.newInstance();
        Method method = targetObject.getClass().getMethod("echo");
        when(methodInvocation.getThis()).thenReturn(targetObject);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.proceed()).then(invocationOnMock -> method.invoke(targetObject));
    }

    static class TestMethod {

        @DataPermission(enable = false)
        public String echo() {
            return "method";
        }

    }

    @DataPermission(enable = false)
    static class TestClass {

        public String echo() {
            return "class";
        }

    }

    static class TestNone {

        public String echo() {
            return "none";
        }

    }

}
