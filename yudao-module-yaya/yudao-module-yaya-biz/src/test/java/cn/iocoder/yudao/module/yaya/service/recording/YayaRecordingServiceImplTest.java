package cn.iocoder.yudao.module.yaya.service.recording;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.recording.YayaRecordingDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.recording.YayaRecordingMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static org.junit.jupiter.api.Assertions.*;

@Import(YayaRecordingServiceImpl.class)
class YayaRecordingServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaRecordingServiceImpl recordingService;
    @Resource
    private YayaRecordingMapper recordingMapper;
    @TempDir
    private Path tempDir;

    @Test
    void createRecordingShouldStoreFileAndPersistMetadata() throws Exception {
        ReflectionTestUtils.setField(recordingService, "storageRoot", tempDir.toString());
        YayaAppRecordingUploadReqVO reqVO = new YayaAppRecordingUploadReqVO()
                .setFile(new MockMultipartFile("file", "answer.webm", "audio/webm", "audio-bytes".getBytes()))
                .setTopicId(146L)
                .setQuestionId(10L)
                .setAttemptId(20L)
                .setDurationSeconds(12.4);

        YayaAppRecordingRespVO response = recordingService.createRecording(10001L, reqVO);

        YayaRecordingDO recording = recordingMapper.selectById(response.getId());
        assertEquals(10001L, recording.getMemberUserId());
        assertEquals(146L, recording.getTopicId());
        assertEquals(10L, recording.getQuestionId());
        assertEquals(20L, recording.getAttemptId());
        assertEquals("audio/webm", recording.getMimeType());
        assertEquals(11L, recording.getSizeBytes());
        assertEquals("stored", recording.getStatus());
        assertEquals(12.4, recording.getDurationSeconds());
        assertTrue(Files.exists(Path.of(recording.getStoragePath())));
        assertEquals(recording.getId(), response.getId());
        assertEquals(recording.getStoragePath(), response.getStoragePath());
    }

    @Test
    void createRecordingShouldRequireMember() {
        YayaAppRecordingUploadReqVO reqVO = new YayaAppRecordingUploadReqVO()
                .setFile(new MockMultipartFile("file", "answer.webm", "audio/webm", "audio".getBytes()));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> recordingService.createRecording(null, reqVO));

        assertEquals(YAYA_MEMBER_NOT_LOGIN.getCode(), exception.getCode());
    }

}
