package cn.iocoder.yudao.module.yaya.service.ai;

import java.util.Map;

public interface YayaAiTaskService {

    Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId);

    Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId, Long evaluationId);

    Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId, Long evaluationId,
                              Map<String, Object> options);

    void pollTaskResult(Long taskId);

    void cancelTask(Long taskId);

}
