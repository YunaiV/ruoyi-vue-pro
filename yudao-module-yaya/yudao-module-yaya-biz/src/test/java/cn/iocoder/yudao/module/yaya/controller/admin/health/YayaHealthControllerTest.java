package cn.iocoder.yudao.module.yaya.controller.admin.health;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YayaHealthControllerTest {

    @Test
    void healthShouldReturnOk() {
        YayaHealthController controller = new YayaHealthController();

        CommonResult<String> result = controller.health();

        assertEquals(0, result.getCode());
        assertEquals("ok", result.getData());
        assertEquals("", result.getMsg());
    }

    @Test
    void healthShouldAllowAnonymousAccess() throws NoSuchMethodException {
        Method method = YayaHealthController.class.getMethod("health");

        assertEquals(PermitAll.class, method.getAnnotation(PermitAll.class).annotationType());
    }

    @Test
    void healthShouldIgnoreTenantContext() throws NoSuchMethodException {
        Method method = YayaHealthController.class.getMethod("health");

        assertEquals(TenantIgnore.class, method.getAnnotation(TenantIgnore.class).annotationType());
    }

}
