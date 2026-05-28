package cn.iocoder.yudao.module.yaya.controller.admin.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicPublishStatusReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaQuestionSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicQuestionsSaveReqVO;
import cn.iocoder.yudao.module.yaya.service.content.YayaContentService;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicDetailResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicListItemResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaTopicControllerTest {

    @Test
    void shouldExposeTopicRoutesUnderYaya() throws NoSuchMethodException {
        RequestMapping mapping = YayaTopicController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya"}, mapping.value());

        assertGetMapping(method("getTopicPage", YayaTopicPageReqVO.class), "/topics");
        assertGetMapping(method("getTopic", Long.class), "/topics/{id}");
        assertPostMapping(method("createTopic", YayaTopicSaveReqVO.class), "/topics");
        assertPatchMapping(method("updateTopic", Long.class, YayaTopicSaveReqVO.class), "/topics/{id}");
        assertPutMapping(method("replaceQuestions", Long.class, YayaTopicQuestionsSaveReqVO.class), "/topics/{id}/questions");
        assertPatchMapping(method("updatePublishStatus", Long.class, YayaTopicPublishStatusReqVO.class), "/topics/{id}/publish-status");
    }

    @Test
    void shouldRequireConfiguredPermissions() throws NoSuchMethodException {
        assertPermission(method("getTopicPage", YayaTopicPageReqVO.class), "yaya:topic:query");
        assertPermission(method("getTopic", Long.class), "yaya:topic:query");
        assertPermission(method("createTopic", YayaTopicSaveReqVO.class), "yaya:topic:create");
        assertPermission(method("updateTopic", Long.class, YayaTopicSaveReqVO.class), "yaya:topic:update");
        assertPermission(method("replaceQuestions", Long.class, YayaTopicQuestionsSaveReqVO.class), "yaya:topic:update");
        assertPermission(method("updatePublishStatus", Long.class, YayaTopicPublishStatusReqVO.class), "yaya:topic:publish");
    }

    @Test
    void getTopicPageShouldReturnServicePage() {
        YayaContentService service = mock(YayaContentService.class);
        YayaTopicController controller = controller(service);
        PageResult<YayaTopicListItemResp> page = new PageResult<>(List.of(new YayaTopicListItemResp().setId(1L)), 1L);
        when(service.getTopicPage(any())).thenReturn(page);

        CommonResult<PageResult<YayaTopicListItemResp>> result = controller.getTopicPage(new YayaTopicPageReqVO());

        assertEquals(0, result.getCode());
        assertEquals(1L, result.getData().getTotal());
    }

    @Test
    void createUpdateQuestionsAndPublishShouldDelegate() {
        YayaContentService service = mock(YayaContentService.class);
        YayaTopicController controller = controller(service);
        when(service.createTopic(any())).thenReturn(10L);
        YayaTopicSaveReqVO topic = new YayaTopicSaveReqVO().setSeasonId(1L).setPart(1).setStableKey("work");
        YayaTopicQuestionsSaveReqVO questions = new YayaTopicQuestionsSaveReqVO()
                .setQuestions(List.of(new YayaQuestionSaveReqVO().setPromptEn("What do you do?")));

        assertEquals(10L, controller.createTopic(topic).getData());
        assertTrue(controller.updateTopic(10L, topic).getData());
        assertTrue(controller.replaceQuestions(10L, questions).getData());
        assertTrue(controller.updatePublishStatus(10L, new YayaTopicPublishStatusReqVO().setPublishStatus("published")).getData());
        verify(service).updateTopic(eq(10L), any());
        verify(service).replaceQuestions(eq(10L), anyList());
        verify(service).updateTopicPublishStatus(10L, "published");
    }

    @Test
    void getTopicShouldReturnDetail() {
        YayaContentService service = mock(YayaContentService.class);
        YayaTopicController controller = controller(service);
        when(service.getTopicDetail(11L)).thenReturn(new YayaTopicDetailResp().setPromptEn("prompt"));

        CommonResult<YayaTopicDetailResp> result = controller.getTopic(11L);

        assertEquals("prompt", result.getData().getPromptEn());
    }

    private static YayaTopicController controller(YayaContentService service) {
        YayaTopicController controller = new YayaTopicController();
        ReflectionTestUtils.setField(controller, "contentService", service);
        return controller;
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaTopicController.class.getMethod(name, parameterTypes);
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

    private static void assertPutMapping(Method method, String path) {
        PutMapping annotation = method.getAnnotation(PutMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

}
