package cn.iocoder.yudao.module.yaya.controller.app.recording;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;
import cn.iocoder.yudao.module.yaya.service.recording.YayaRecordingService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaAppRecordingControllerTest {

    @Test
    void shouldExposeRecordingUploadRoute() throws NoSuchMethodException {
        RequestMapping mapping = YayaAppRecordingController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/recordings"}, mapping.value());

        PostMapping postMapping = YayaAppRecordingController.class
                .getMethod("createRecording", YayaAppRecordingUploadReqVO.class)
                .getAnnotation(PostMapping.class);
        assertNotNull(postMapping);
        assertArrayEquals(new String[]{""}, postMapping.value());
    }

    @Test
    void shouldDelegateUploadToRecordingService() throws Exception {
        YayaRecordingService service = mock(YayaRecordingService.class);
        YayaAppRecordingController controller = new YayaAppRecordingController();
        ReflectionTestUtils.setField(controller, "recordingService", service);
        YayaAppRecordingUploadReqVO reqVO = new YayaAppRecordingUploadReqVO()
                .setFile(new MockMultipartFile("file", "answer.webm", "audio/webm", "audio".getBytes()))
                .setTopicId(146L)
                .setDurationSeconds(12.4);
        YayaAppRecordingRespVO respVO = new YayaAppRecordingRespVO().setId(20001L).setStatus("stored");
        when(service.createRecording(null, reqVO)).thenReturn(respVO);

        CommonResult<YayaAppRecordingRespVO> result = controller.createRecording(reqVO);

        assertEquals(20001L, result.getData().getId());
        assertEquals("stored", result.getData().getStatus());
        verify(service).createRecording(null, reqVO);
    }

}
