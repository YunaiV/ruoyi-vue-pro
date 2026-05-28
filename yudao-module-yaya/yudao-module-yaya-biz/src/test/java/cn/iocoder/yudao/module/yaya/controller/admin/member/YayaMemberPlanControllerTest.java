package cn.iocoder.yudao.module.yaya.controller.admin.member;

import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanRespVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanStatusReqVO;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaMemberPlanControllerTest {

    @Test
    void shouldExposeMemberPlanRoutes() throws NoSuchMethodException {
        RequestMapping mapping = YayaMemberPlanController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/member-plans"}, mapping.value());

        assertGetMapping(method("getPlanList"), "");
        assertPostMapping(method("createPlan", YayaMemberPlanSaveReqVO.class), "");
        assertPatchMapping(method("updatePlan", Long.class, YayaMemberPlanSaveReqVO.class), "/{id}");
        assertPatchMapping(method("updatePlanStatus", Long.class, YayaMemberPlanStatusReqVO.class), "/{id}/status");
    }

    @Test
    void shouldRequireConfiguredPermissions() throws NoSuchMethodException {
        assertPermission(method("getPlanList"), "yaya:member-plan:query");
        assertPermission(method("createPlan", YayaMemberPlanSaveReqVO.class), "yaya:member-plan:create");
        assertPermission(method("updatePlan", Long.class, YayaMemberPlanSaveReqVO.class), "yaya:member-plan:update");
        assertPermission(method("updatePlanStatus", Long.class, YayaMemberPlanStatusReqVO.class), "yaya:member-plan:update");
    }

    @Test
    void shouldDelegateToService() {
        YayaEntitlementService service = mock(YayaEntitlementService.class);
        YayaMemberPlanController controller = new YayaMemberPlanController();
        ReflectionTestUtils.setField(controller, "entitlementService", service);
        YayaMemberPlanSaveReqVO reqVO = new YayaMemberPlanSaveReqVO().setPlanKey("monthly-pro");
        when(service.getPlanList()).thenReturn(List.of(new YayaMemberPlanRespVO().setPlanKey("monthly-pro")));
        when(service.createPlan(reqVO)).thenReturn(10L);

        assertEquals("monthly-pro", controller.getPlanList().getData().get(0).getPlanKey());
        assertEquals(10L, controller.createPlan(reqVO).getData());
        assertTrue(controller.updatePlan(10L, reqVO).getData());
        assertTrue(controller.updatePlanStatus(10L, new YayaMemberPlanStatusReqVO().setActive(1)).getData());
        verify(service).updatePlan(10L, reqVO);
        verify(service).updatePlanStatus(10L, 1);
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaMemberPlanController.class.getMethod(name, parameterTypes);
    }

    private static void assertPermission(Method method, String permission) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertEquals("@ss.hasPermission('" + permission + "')", annotation.value());
    }

    private static void assertGetMapping(Method method, String path) {
        GetMapping annotation = method.getAnnotation(GetMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

    private static void assertPostMapping(Method method, String path) {
        PostMapping annotation = method.getAnnotation(PostMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

    private static void assertPatchMapping(Method method, String path) {
        PatchMapping annotation = method.getAnnotation(PatchMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

}
