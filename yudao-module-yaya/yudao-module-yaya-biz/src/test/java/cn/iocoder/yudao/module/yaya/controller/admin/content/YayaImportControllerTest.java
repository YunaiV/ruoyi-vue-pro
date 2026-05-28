package cn.iocoder.yudao.module.yaya.controller.admin.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaImportResultRespVO;
import cn.iocoder.yudao.module.yaya.service.content.YayaImportService;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaImportControllerTest {

    @Test
    void shouldExposeImportRoutesUnderYaya() throws NoSuchMethodException {
        RequestMapping mapping = YayaImportController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya"}, mapping.value());

        assertPostMapping(method("previewImport", String.class), "/import-batches/{season}:preview");
        assertPostMapping(method("runImport", String.class), "/import-batches/{season}:run");
    }

    @Test
    void shouldRequireConfiguredPermissions() throws NoSuchMethodException {
        assertPermission(method("previewImport", String.class), "yaya:import:preview");
        assertPermission(method("runImport", String.class), "yaya:import:run");
    }

    @Test
    void previewAndRunShouldReturnServiceResult() {
        YayaImportService service = mock(YayaImportService.class);
        when(service.previewImport("26Q1")).thenReturn(new YayaImportResultResp()
                .setSeasonKey("26Q1").setTopics(39).setQuestions(227).setErrors(List.of()));
        when(service.runImport("26Q1")).thenReturn(new YayaImportResultResp()
                .setSeasonKey("26Q1").setTopics(39).setQuestions(227).setErrors(List.of()));
        YayaImportController controller = new YayaImportController();
        ReflectionTestUtils.setField(controller, "importService", service);

        CommonResult<YayaImportResultRespVO> preview = controller.previewImport("26Q1");
        CommonResult<YayaImportResultRespVO> run = controller.runImport("26Q1");

        assertEquals(39, preview.getData().getTopics());
        assertEquals(227, run.getData().getQuestions());
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaImportController.class.getMethod(name, parameterTypes);
    }

    private static void assertPermission(Method method, String permission) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertEquals("@ss.hasPermission('" + permission + "')", annotation.value());
    }

    private static void assertPostMapping(Method method, String path) {
        PostMapping annotation = method.getAnnotation(PostMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

}
