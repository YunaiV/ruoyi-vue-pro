package cn.iocoder.yudao.module.yaya.service.evaluation;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.evaluation.YayaEvaluationDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.recording.YayaRecordingDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.evaluation.YayaEvaluationMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.recording.YayaRecordingMapper;
import cn.iocoder.yudao.module.yaya.service.ai.YayaAiTaskService;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Import(YayaEvaluationServiceImpl.class)
class YayaEvaluationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaEvaluationServiceImpl evaluationService;
    @Resource
    private YayaEvaluationMapper evaluationMapper;
    @Resource
    private YayaRecordingMapper recordingMapper;
    @MockitoBean
    private YayaAiTaskService aiTaskService;
    @MockitoBean
    private YayaEntitlementService entitlementService;

    @Test
    void createEvaluationShouldPersistEvaluationAndCreateAiTask() {
        Long recordingId = insertRecording(10001L, 146L);
        when(aiTaskService.createEvaluationTask(eq(10001L), eq(recordingId), eq(146L), anyLong(),
                eq(Map.of("runTextRoute", true)))).thenReturn(40001L);
        YayaAppEvaluationCreateReqVO reqVO = new YayaAppEvaluationCreateReqVO()
                .setRecordingId(recordingId)
                .setTopicId(146L)
                .setOptions(Map.of("runTextRoute", true));

        YayaAppEvaluationRespVO response = evaluationService.createEvaluation(10001L, reqVO);

        YayaEvaluationDO evaluation = evaluationMapper.selectById(response.getId());
        assertEquals(10001L, evaluation.getMemberUserId());
        assertEquals(recordingId, evaluation.getRecordingId());
        assertEquals(146L, evaluation.getTopicId());
        assertEquals(40001L, evaluation.getAiTaskId());
        assertEquals("PENDING", evaluation.getStatus());
        assertEquals(40001L, response.getAiTaskId());
        verify(entitlementService).requireActiveEntitlement(10001L);
        verify(aiTaskService).createEvaluationTask(10001L, recordingId, 146L, response.getId(),
                Map.of("runTextRoute", true));
    }

    @Test
    void getEvaluationShouldReturnOnlyOwnersEvaluation() {
        Long recordingId = insertRecording(10001L, 146L);
        YayaEvaluationDO evaluation = new YayaEvaluationDO();
        evaluation.setMemberUserId(10001L);
        evaluation.setRecordingId(recordingId);
        evaluation.setTopicId(146L);
        evaluation.setAiTaskId(40001L);
        evaluation.setStatus("SUCCEEDED");
        evaluation.setScores(Map.of("overall", 6.5));
        evaluationMapper.insert(evaluation);

        YayaAppEvaluationRespVO response = evaluationService.getEvaluation(10001L, evaluation.getId());

        assertEquals(evaluation.getId(), response.getId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals(6.5, response.getScores().get("overall"));
    }

    @Test
    void createEvaluationShouldRequireMember() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> evaluationService.createEvaluation(null, new YayaAppEvaluationCreateReqVO()
                        .setRecordingId(20001L)
                        .setTopicId(146L)));

        assertEquals(YAYA_MEMBER_NOT_LOGIN.getCode(), exception.getCode());
        verifyNoInteractions(aiTaskService);
        verifyNoInteractions(entitlementService);
    }

    @Test
    void createEvaluationShouldStopWhenEntitlementIsMissing() {
        Long recordingId = insertRecording(10001L, 146L);
        doThrow(new ServiceException(1_050_006_003, "Yaya 会员权益不足")).when(entitlementService)
                .requireActiveEntitlement(10001L);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> evaluationService.createEvaluation(10001L, new YayaAppEvaluationCreateReqVO()
                        .setRecordingId(recordingId)
                        .setTopicId(146L)));

        assertEquals(1_050_006_003, exception.getCode());
        verify(entitlementService).requireActiveEntitlement(10001L);
        verifyNoInteractions(aiTaskService);
    }

    private Long insertRecording(Long memberUserId, Long topicId) {
        YayaRecordingDO recording = new YayaRecordingDO();
        recording.setMemberUserId(memberUserId);
        recording.setTopicId(topicId);
        recording.setStoragePath("/tmp/answer.webm");
        recording.setMimeType("audio/webm");
        recording.setSizeBytes(11L);
        recording.setStatus("stored");
        recordingMapper.insert(recording);
        return recording.getId();
    }

}
