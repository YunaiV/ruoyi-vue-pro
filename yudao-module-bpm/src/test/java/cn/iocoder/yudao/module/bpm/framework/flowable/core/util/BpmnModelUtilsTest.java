package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskRejectHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.flowable.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BpmnModelUtils} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmnModelUtilsTest {

    @Test
    public void testCandidateElements_success() {
        // 准备参数
        UserTask userTask = new UserTask();

        // 调用
        BpmnModelUtils.addCandidateElements(BpmTaskCandidateStrategyEnum.USER.getStrategy(), "1,2", userTask);
        // 断言
        assertEquals(BpmTaskCandidateStrategyEnum.USER.getStrategy(),
                BpmnModelUtils.parseCandidateStrategy(userTask));
        assertEquals("1,2", BpmnModelUtils.parseCandidateParam(userTask));
    }

    @Test
    public void testCandidateElements_empty() {
        // 准备参数
        UserTask userTask = new UserTask();

        // 调用
        BpmnModelUtils.addCandidateElements(null, null, userTask);
        // 断言
        assertNull(BpmnModelUtils.parseCandidateStrategy(userTask));
        assertNull(BpmnModelUtils.parseCandidateParam(userTask));
    }

    @Test
    public void testRejectElements_success() {
        // 准备参数
        UserTask userTask = new UserTask();
        BpmSimpleModelNodeVO.RejectHandler rejectHandler = new BpmSimpleModelNodeVO.RejectHandler()
                .setType(BpmUserTaskRejectHandlerTypeEnum.RETURN_USER_TASK.getType())
                .setReturnNodeId("approve");

        // 调用
        BpmnModelUtils.addTaskRejectElements(rejectHandler, userTask);
        // 断言
        assertEquals(BpmUserTaskRejectHandlerTypeEnum.RETURN_USER_TASK,
                BpmnModelUtils.parseRejectHandlerType(userTask));
        assertEquals("approve", BpmnModelUtils.parseReturnTaskId(userTask));
    }

    @Test
    public void testExtensionElement_emptyValue_notAdd() {
        // 准备参数
        UserTask userTask = new UserTask();

        // 调用
        BpmnModelUtils.addExtensionElement(userTask, "remark", (String) null);
        BpmnModelUtils.addExtensionElementJson(userTask, "config", null);
        BpmnModelUtils.addExtensionElement(userTask, "attributes", Collections.emptyMap());
        // 断言
        assertNull(BpmnModelUtils.parseExtensionElement(userTask, "remark"));
        assertNull(BpmnModelUtils.parseExtensionElement(userTask, "config"));
    }

}
