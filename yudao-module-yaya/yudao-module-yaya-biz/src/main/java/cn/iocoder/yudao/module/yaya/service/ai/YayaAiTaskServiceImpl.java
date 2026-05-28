package cn.iocoder.yudao.module.yaya.service.ai;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.yaya.dal.dataobject.ai.YayaAiTaskDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.ai.YayaAiTaskMapper;
import cn.iocoder.yudao.module.yaya.framework.ai.YayaAiClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_AI_TASK_NOT_EXISTS;

@Service
@Validated
public class YayaAiTaskServiceImpl implements YayaAiTaskService {

    private static final String TASK_TYPE_EVALUATION = "evaluation";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Resource
    private YayaAiTaskMapper taskMapper;
    @Resource
    private YayaAiClient aiClient;

    @Override
    public Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId) {
        return createEvaluationTask(memberUserId, recordingId, topicId, null);
    }

    @Override
    public Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId, Long evaluationId) {
        return createEvaluationTask(memberUserId, recordingId, topicId, evaluationId, null);
    }

    @Override
    public Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId, Long evaluationId,
                                     Map<String, Object> options) {
        YayaAiTaskDO task = new YayaAiTaskDO();
        task.setMemberUserId(memberUserId);
        task.setRecordingId(recordingId);
        task.setTopicId(topicId);
        task.setTaskKey("pending-" + UUID.randomUUID());
        task.setTaskType(TASK_TYPE_EVALUATION);
        task.setStatus(STATUS_PENDING);
        task.setRequest(Collections.emptyMap());
        taskMapper.insert(task);

        String taskKey = String.valueOf(task.getId());
        String evaluationKey = evaluationId == null ? taskKey : String.valueOf(evaluationId);
        YayaAiClient.EvaluationCreateRequest request = buildEvaluationRequest(taskKey, evaluationKey,
                memberUserId, recordingId, topicId, options);
        task.setTaskKey(taskKey);
        task.setRequest(toMap(request));
        taskMapper.updateById(task);

        YayaAiClient.EvaluationAcceptedResponse response = aiClient.createEvaluation(request, null);
        YayaAiTaskDO update = new YayaAiTaskDO();
        update.setId(task.getId());
        update.setStatus(defaultString(response.getStatus(), STATUS_PENDING));
        update.setResponse(toMap(response));
        update.setAcceptedAt(LocalDateTime.now());
        taskMapper.updateById(update);
        return task.getId();
    }

    @Override
    public void pollTaskResult(Long taskId) {
        YayaAiTaskDO task = validateTaskExists(taskId);
        YayaAiClient.EvaluationTaskResponse response = aiClient.getEvaluation(task.getTaskKey(), null);

        YayaAiTaskDO update = new YayaAiTaskDO();
        update.setId(taskId);
        update.setStatus(defaultString(response.getStatus(), task.getStatus()));
        update.setResponse(toMap(response));
        update.setResult(response.getResult());
        update.setError(response.getError());
        if (isTerminal(response.getStatus())) {
            update.setCompletedAt(LocalDateTime.now());
        }
        taskMapper.updateById(update);
    }

    @Override
    public void cancelTask(Long taskId) {
        validateTaskExists(taskId);
        YayaAiTaskDO update = new YayaAiTaskDO();
        update.setId(taskId);
        update.setStatus(STATUS_CANCELLED);
        update.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(update);
    }

    private YayaAiTaskDO validateTaskExists(Long taskId) {
        YayaAiTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(YAYA_AI_TASK_NOT_EXISTS);
        }
        return task;
    }

    private YayaAiClient.EvaluationCreateRequest buildEvaluationRequest(String taskKey, String evaluationKey,
                                                                        Long memberUserId,
                                                                        Long recordingId, Long topicId,
                                                                        Map<String, Object> options) {
        return new YayaAiClient.EvaluationCreateRequest()
                .setTaskId(taskKey)
                .setEvaluationId(evaluationKey)
                .setRecordingId(String.valueOf(recordingId))
                .setUserId(String.valueOf(memberUserId))
                .setTopicId(String.valueOf(topicId))
                .setOptions(normalizeOptions(options));
    }

    private static Map<String, Object> normalizeOptions(Map<String, Object> options) {
        return Map.of(
                "run_text_route", optionValue(options, "runTextRoute", "run_text_route"),
                "run_audio_route", optionValue(options, "runAudioRoute", "run_audio_route"),
                "run_pronunciation_route", optionValue(options, "runPronunciationRoute", "run_pronunciation_route"),
                "run_improvement", optionValue(options, "runImprovement", "run_improvement"));
    }

    private static Object optionValue(Map<String, Object> options, String camelKey, String snakeKey) {
        if (options == null) {
            return true;
        }
        if (options.containsKey(camelKey)) {
            return options.get(camelKey);
        }
        return options.getOrDefault(snakeKey, true);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Collections.emptyMap();
        }
        return JsonUtils.parseObject(JsonUtils.toJsonString(value), Map.class);
    }

    private static boolean isTerminal(String status) {
        return "SUCCEEDED".equals(status) || "FAILED".equals(status) || STATUS_CANCELLED.equals(status);
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

}
