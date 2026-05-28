package cn.iocoder.yudao.module.yaya.controller.app.practice;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppFavoriteCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.service.practice.YayaPracticeService;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaAppPracticeControllerTest {

    @Test
    void shouldExposePracticeRoutesUnderYayaPractice() throws NoSuchMethodException {
        RequestMapping mapping = YayaAppPracticeController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/practice"}, mapping.value());

        assertGetMapping(method("getTopicPage", YayaAppPracticeTopicPageReqVO.class), "/topics");
        assertGetMapping(method("getTopic", Long.class), "/topics/{id}");
        assertPostMapping(method("createFavorite", YayaAppFavoriteCreateReqVO.class), "/favorites");
        assertPostMapping(method("createAttempt", YayaAppPracticeAttemptCreateReqVO.class), "/attempts");
    }

    @Test
    void readEndpointsShouldAllowAnonymousAccess() throws NoSuchMethodException {
        assertNotNull(method("getTopicPage", YayaAppPracticeTopicPageReqVO.class).getAnnotation(PermitAll.class));
        assertNotNull(method("getTopic", Long.class).getAnnotation(PermitAll.class));
        assertNotNull(method("getTopicPage", YayaAppPracticeTopicPageReqVO.class).getAnnotation(TenantIgnore.class));
        assertNotNull(method("getTopic", Long.class).getAnnotation(TenantIgnore.class));
        assertNull(method("createFavorite", YayaAppFavoriteCreateReqVO.class).getAnnotation(PermitAll.class));
        assertNull(method("createAttempt", YayaAppPracticeAttemptCreateReqVO.class).getAnnotation(PermitAll.class));
    }

    @Test
    void shouldDelegateReadsAndWritesToPracticeService() {
        YayaPracticeService service = mock(YayaPracticeService.class);
        YayaAppPracticeController controller = controller(service);
        YayaAppPracticeTopicPageReqVO pageReq = new YayaAppPracticeTopicPageReqVO().setSeason("26Q1").setPart(1);
        PageResult<YayaAppPracticeTopicRespVO> page =
                new PageResult<>(List.of(new YayaAppPracticeTopicRespVO().setId(1L)), 1L);
        when(service.getTopicPage(pageReq, null)).thenReturn(page);
        YayaAppPracticeTopicDetailRespVO detail = new YayaAppPracticeTopicDetailRespVO();
        detail.setId(1L);
        when(service.getTopicDetail(1L, null)).thenReturn(detail);
        when(service.createFavorite(null, 1L)).thenReturn(10L);
        YayaAppPracticeAttemptCreateReqVO attemptReq = new YayaAppPracticeAttemptCreateReqVO().setTopicId(1L);
        when(service.createAttempt(null, attemptReq)).thenReturn(20L);

        CommonResult<PageResult<YayaAppPracticeTopicRespVO>> pageResult = controller.getTopicPage(pageReq);
        CommonResult<YayaAppPracticeTopicDetailRespVO> detailResult = controller.getTopic(1L);
        CommonResult<Long> favoriteResult = controller.createFavorite(new YayaAppFavoriteCreateReqVO().setTopicId(1L));
        CommonResult<Long> attemptResult = controller.createAttempt(attemptReq);

        assertEquals(1L, pageResult.getData().getTotal());
        assertEquals(1L, detailResult.getData().getId());
        assertEquals(10L, favoriteResult.getData());
        assertEquals(20L, attemptResult.getData());
        verify(service).getTopicPage(pageReq, null);
        verify(service).getTopicDetail(1L, null);
        verify(service).createFavorite(null, 1L);
        verify(service).createAttempt(null, attemptReq);
    }

    private static YayaAppPracticeController controller(YayaPracticeService service) {
        YayaAppPracticeController controller = new YayaAppPracticeController();
        ReflectionTestUtils.setField(controller, "practiceService", service);
        return controller;
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaAppPracticeController.class.getMethod(name, parameterTypes);
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

}
