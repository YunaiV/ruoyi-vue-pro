package cn.iocoder.yudao.module.yaya.controller.app.member;

import cn.iocoder.yudao.module.yaya.controller.app.member.vo.YayaAppEntitlementRespVO;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaAppEntitlementControllerTest {

    @Test
    void shouldExposeCurrentEntitlementRoute() throws NoSuchMethodException {
        RequestMapping mapping = YayaAppEntitlementController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/entitlements"}, mapping.value());

        GetMapping getMapping = method("getMyEntitlement").getAnnotation(GetMapping.class);
        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/me"}, getMapping.value());
    }

    @Test
    void shouldDelegateToService() {
        YayaEntitlementService service = mock(YayaEntitlementService.class);
        YayaAppEntitlementController controller = new YayaAppEntitlementController();
        ReflectionTestUtils.setField(controller, "entitlementService", service);
        LocalDateTime endsAt = LocalDateTime.now().plusDays(30);
        when(service.getMyEntitlement(null)).thenReturn(new YayaAppEntitlementRespVO()
                .setActive(true)
                .setPlanKey("monthly-pro")
                .setEndsAt(endsAt));

        YayaAppEntitlementRespVO response = controller.getMyEntitlement().getData();

        assertTrue(response.getActive());
        assertEquals("monthly-pro", response.getPlanKey());
        assertEquals(endsAt, response.getEndsAt());
        verify(service).getMyEntitlement(null);
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaAppEntitlementController.class.getMethod(name, parameterTypes);
    }

}
