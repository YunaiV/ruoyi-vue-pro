package cn.iocoder.yudao.module.yaya.service.evaluation;

import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.evaluation.YayaEvaluationDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.recording.YayaRecordingDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.evaluation.YayaEvaluationMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.recording.YayaRecordingMapper;
import cn.iocoder.yudao.module.yaya.service.ai.YayaAiTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_EVALUATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_RECORDING_NOT_EXISTS;

@Service
@Validated
public class YayaEvaluationServiceImpl implements YayaEvaluationService {

    private static final String STATUS_PENDING = "PENDING";

    @Resource
    private YayaEvaluationMapper evaluationMapper;
    @Resource
    private YayaRecordingMapper recordingMapper;
    @Resource
    private YayaAiTaskService aiTaskService;

    @Override
    public YayaAppEvaluationRespVO createEvaluation(Long memberUserId, YayaAppEvaluationCreateReqVO reqVO) {
        requireMember(memberUserId);
        YayaRecordingDO recording = validateRecording(memberUserId, reqVO.getRecordingId());
        Long topicId = reqVO.getTopicId() == null ? recording.getTopicId() : reqVO.getTopicId();

        YayaEvaluationDO evaluation = new YayaEvaluationDO();
        evaluation.setMemberUserId(memberUserId);
        evaluation.setRecordingId(reqVO.getRecordingId());
        evaluation.setTopicId(topicId);
        evaluation.setStatus(STATUS_PENDING);
        evaluation.setProvider("");
        evaluation.setModel("");
        evaluation.setScores(Collections.emptyMap());
        evaluationMapper.insert(evaluation);

        Long aiTaskId = aiTaskService.createEvaluationTask(memberUserId, reqVO.getRecordingId(), topicId,
                evaluation.getId(), reqVO.getOptions());
        YayaEvaluationDO update = new YayaEvaluationDO();
        update.setId(evaluation.getId());
        update.setAiTaskId(aiTaskId);
        update.setStatus(STATUS_PENDING);
        evaluationMapper.updateById(update);
        evaluation.setAiTaskId(aiTaskId);
        return toResp(evaluation);
    }

    @Override
    public YayaAppEvaluationRespVO getEvaluation(Long memberUserId, Long evaluationId) {
        requireMember(memberUserId);
        return toResp(validateEvaluation(memberUserId, evaluationId));
    }

    @Override
    public YayaAppEvaluationRespVO createPolishPack(Long memberUserId, Long evaluationId) {
        requireMember(memberUserId);
        return toResp(validateEvaluation(memberUserId, evaluationId)).setPolishPackStatus(STATUS_PENDING);
    }

    private YayaRecordingDO validateRecording(Long memberUserId, Long recordingId) {
        YayaRecordingDO recording = recordingMapper.selectById(recordingId);
        if (recording == null || !Objects.equals(recording.getMemberUserId(), memberUserId)) {
            throw exception(YAYA_RECORDING_NOT_EXISTS);
        }
        return recording;
    }

    private YayaEvaluationDO validateEvaluation(Long memberUserId, Long evaluationId) {
        YayaEvaluationDO evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null || !Objects.equals(evaluation.getMemberUserId(), memberUserId)) {
            throw exception(YAYA_EVALUATION_NOT_EXISTS);
        }
        return evaluation;
    }

    private static YayaAppEvaluationRespVO toResp(YayaEvaluationDO evaluation) {
        return new YayaAppEvaluationRespVO()
                .setId(evaluation.getId())
                .setRecordingId(evaluation.getRecordingId())
                .setTopicId(evaluation.getTopicId())
                .setAiTaskId(evaluation.getAiTaskId())
                .setStatus(evaluation.getStatus())
                .setProvider(evaluation.getProvider())
                .setModel(evaluation.getModel())
                .setScoreOverall(evaluation.getScoreOverall())
                .setScores(evaluation.getScores())
                .setReport(evaluation.getReport());
    }

    private static void requireMember(Long memberUserId) {
        if (memberUserId == null) {
            throw exception(YAYA_MEMBER_NOT_LOGIN);
        }
    }

}
