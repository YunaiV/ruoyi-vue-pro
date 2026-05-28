package cn.iocoder.yudao.module.yaya.controller.app.evaluation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.service.evaluation.YayaEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaAppEvaluationControllerTest {

    @Test
    void shouldExposeEvaluationRoutes() throws NoSuchMethodException {
        RequestMapping mapping = YayaAppEvaluationController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/evaluations"}, mapping.value());

        assertPostMapping(method("createEvaluation", YayaAppEvaluationCreateReqVO.class), "");
        assertGetMapping(method("getEvaluation", Long.class), "/{evaluationId}");
        assertPostMapping(method("createPolishPack", Long.class), "/{evaluationId}/polish-pack");
    }

    @Test
    void shouldDelegateToEvaluationService() {
        YayaEvaluationService service = mock(YayaEvaluationService.class);
        YayaAppEvaluationController controller = new YayaAppEvaluationController();
        ReflectionTestUtils.setField(controller, "evaluationService", service);
        YayaAppEvaluationCreateReqVO reqVO = new YayaAppEvaluationCreateReqVO()
                .setRecordingId(20001L)
                .setTopicId(146L)
                .setOptions(Map.of("runTextRoute", true));
        YayaAppEvaluationRespVO created = new YayaAppEvaluationRespVO()
                .setId(30001L)
                .setAiTaskId(40001L)
                .setStatus("PENDING");
        when(service.createEvaluation(null, reqVO)).thenReturn(created);
        when(service.getEvaluation(null, 30001L)).thenReturn(created);
        when(service.createPolishPack(null, 30001L)).thenReturn(created);

        CommonResult<YayaAppEvaluationRespVO> createResult = controller.createEvaluation(reqVO);
        CommonResult<YayaAppEvaluationRespVO> detailResult = controller.getEvaluation(30001L);
        CommonResult<YayaAppEvaluationRespVO> polishResult = controller.createPolishPack(30001L);

        assertEquals(30001L, createResult.getData().getId());
        assertEquals(40001L, detailResult.getData().getAiTaskId());
        assertEquals("PENDING", polishResult.getData().getStatus());
        verify(service).createEvaluation(null, reqVO);
        verify(service).getEvaluation(null, 30001L);
        verify(service).createPolishPack(null, 30001L);
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaAppEvaluationController.class.getMethod(name, parameterTypes);
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
