package cn.iocoder.yudao.module.bpm.service.comment;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.comment.vo.BpmCommentCreateReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BpmCommentServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmCommentServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmCommentServiceImpl commentService;

    @Mock
    private TaskService taskService;
    @Mock
    private BpmTaskService bpmTaskService;
    @Mock
    private Task task;

    @Test
    public void testGetCommentListByProcessInstanceId_success() {
        // 准备参数
        String processInstanceId = "process-instance-1";

        // 调用
        commentService.getCommentListByProcessInstanceId(processInstanceId);
        // 断言
        verify(taskService).getProcessInstanceComments(processInstanceId);
    }

    @Test
    public void testCreateComment_success() {
        // 准备参数
        BpmCommentCreateReqVO reqVO = new BpmCommentCreateReqVO()
                .setTaskId("task-1").setMessage("请补充附件");
        when(bpmTaskService.validateTaskExists(reqVO.getTaskId())).thenReturn(task);
        when(task.getId()).thenReturn(reqVO.getTaskId());
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用
        commentService.createComment(reqVO);
        // 断言
        verify(taskService).addComment(eq("task-1"), eq("process-instance-1"),
                eq(BpmCommentTypeEnum.COMMENT.getType()), eq("请补充附件"));
    }

    @Test
    public void testCreateComment_withType_success() {
        // 准备参数
        String taskId = "task-1";
        String processInstanceId = "process-instance-1";

        // 调用
        commentService.createComment(taskId, processInstanceId, BpmCommentTypeEnum.APPROVE, "同意");
        // 断言
        verify(taskService).addComment(taskId, processInstanceId, BpmCommentTypeEnum.APPROVE.getType(), "审批通过，原因是：同意");
    }

}
