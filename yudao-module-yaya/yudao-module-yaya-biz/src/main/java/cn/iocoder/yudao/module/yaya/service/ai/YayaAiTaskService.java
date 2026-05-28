package cn.iocoder.yudao.module.yaya.service.ai;

public interface YayaAiTaskService {

    Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId);

    void pollTaskResult(Long taskId);

    void cancelTask(Long taskId);

}
