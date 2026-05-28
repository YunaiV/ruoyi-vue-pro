package cn.iocoder.yudao.module.yaya.service.ai;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.dal.dataobject.ai.YayaAiTaskDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.ai.YayaAiTaskMapper;
import cn.iocoder.yudao.module.yaya.framework.ai.YayaAiClient;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@Import(YayaAiTaskServiceImpl.class)
class YayaAiTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaAiTaskServiceImpl taskService;
    @Resource
    private YayaAiTaskMapper taskMapper;
    @MockitoBean
    private YayaAiClient aiClient;

    @Test
    void createEvaluationTaskShouldPersistTaskAndCallPythonOutsideTransaction() throws NoSuchMethodException {
        when(aiClient.createEvaluation(any(YayaAiClient.EvaluationCreateRequest.class), isNull()))
                .thenReturn(new YayaAiClient.EvaluationAcceptedResponse()
                        .setTaskId("1")
                        .setStatus("PENDING")
                        .setAccepted(true));

        Long taskId = taskService.createEvaluationTask(10001L, 20001L, 146L);

        assertNull(YayaAiTaskServiceImpl.class
                .getMethod("createEvaluationTask", Long.class, Long.class, Long.class)
                .getAnnotation(Transactional.class));
        YayaAiTaskDO task = taskMapper.selectById(taskId);
        assertEquals(10001L, task.getMemberUserId());
        assertEquals(20001L, task.getRecordingId());
        assertEquals(146L, task.getTopicId());
        assertEquals(String.valueOf(taskId), task.getTaskKey());
        assertEquals("PENDING", task.getStatus());
        assertNotNull(task.getAcceptedAt());
        verify(aiClient).createEvaluation(argThat(request ->
                String.valueOf(taskId).equals(request.getTaskId())
                        && String.valueOf(taskId).equals(request.getEvaluationId())
                        && "20001".equals(request.getRecordingId())
                        && "10001".equals(request.getUserId())
                        && "146".equals(request.getTopicId())
        ), isNull());
    }

    @Test
    void createEvaluationTaskShouldForwardEvaluationIdAndRequestedOptions() {
        when(aiClient.createEvaluation(any(YayaAiClient.EvaluationCreateRequest.class), isNull()))
                .thenReturn(new YayaAiClient.EvaluationAcceptedResponse()
                        .setTaskId("1")
                        .setStatus("PENDING")
                        .setAccepted(true));

        Long taskId = taskService.createEvaluationTask(10001L, 20001L, 146L, 30001L,
                Map.of("runTextRoute", false,
                        "run_audio_route", false,
                        "runPronunciationRoute", true,
                        "runImprovement", false));

        assertNotNull(taskMapper.selectById(taskId));
        verify(aiClient).createEvaluation(argThat(request ->
                "30001".equals(request.getEvaluationId())
                        && Boolean.FALSE.equals(request.getOptions().get("run_text_route"))
                        && Boolean.FALSE.equals(request.getOptions().get("run_audio_route"))
                        && Boolean.TRUE.equals(request.getOptions().get("run_pronunciation_route"))
                        && Boolean.FALSE.equals(request.getOptions().get("run_improvement"))
        ), isNull());
    }

    @Test
    void pollTaskResultShouldPersistTerminalResult() {
        Long taskId = insertTask("task-succeeded", "PENDING");
        when(aiClient.getEvaluation("task-succeeded", null))
                .thenReturn(new YayaAiClient.EvaluationTaskResponse()
                        .setTaskId("task-succeeded")
                        .setEvaluationId("task-succeeded")
                        .setStatus("SUCCEEDED")
                        .setResult(Map.of("scores", Map.of("overall", 6.5))));

        taskService.pollTaskResult(taskId);

        YayaAiTaskDO task = taskMapper.selectById(taskId);
        assertEquals("SUCCEEDED", task.getStatus());
        assertEquals(6.5, ((Map<?, ?>) task.getResult().get("scores")).get("overall"));
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void cancelTaskShouldSetCancelledLocally() {
        Long taskId = insertTask("task-cancel", "PENDING");

        taskService.cancelTask(taskId);

        YayaAiTaskDO task = taskMapper.selectById(taskId);
        assertEquals("CANCELLED", task.getStatus());
        assertNotNull(task.getCompletedAt());
        verifyNoInteractions(aiClient);
    }

    private Long insertTask(String taskKey, String status) {
        YayaAiTaskDO task = new YayaAiTaskDO();
        task.setMemberUserId(10001L);
        task.setRecordingId(20001L);
        task.setTopicId(146L);
        task.setTaskKey(taskKey);
        task.setStatus(status);
        task.setRequest(Map.of());
        taskMapper.insert(task);
        return task.getId();
    }

}
