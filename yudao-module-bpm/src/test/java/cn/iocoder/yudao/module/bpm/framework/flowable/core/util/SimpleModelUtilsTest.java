package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserEmptyTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmConditionOpCodeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmDelayTimerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.BaseFlowableUnitTest;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ReceiveTask;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.BOUNDARY_EVENT_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_EVENT_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_JUMP_NODE_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SimpleModelUtils} 的单元测试
 *
 * @author HUIHUI
 */
public class SimpleModelUtilsTest extends BaseFlowableUnitTest {

    @Test
    public void testBuildBpmnModel_startUserApproveEnd() {
        // 准备参数
        BpmSimpleModelNodeVO approveNode = node("approve", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setName("审批").setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod())
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1,2");
        BpmSimpleModelNodeVO startUserNode = node("start-user", BpmSimpleModelNodeTypeEnum.START_USER_NODE)
                .setName("发起人").setChildNode(approveNode);
        approveNode.setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE).setName("结束"));

        // 调用
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel("process", "测试流程", startUserNode);
        Process process = bpmnModel.getMainProcess();
        // 断言
        assertEquals("process", process.getId());
        assertEquals("测试流程", process.getName());
        assertNotNull(process.getFlowElement(START_EVENT_NODE_ID));
        assertInstanceOf(UserTask.class, process.getFlowElement("start-user"));
        UserTask approveTask = (UserTask) process.getFlowElement("approve");
        assertEquals(BpmTaskCandidateStrategyEnum.USER.getStrategy(), BpmnModelUtils.parseCandidateStrategy(approveTask));
        assertEquals("1,2", BpmnModelUtils.parseCandidateParam(approveTask));
        assertInstanceOf(EndEvent.class, process.getFlowElement("end"));
        assertTrue(process.getFlowElements().stream().anyMatch(item -> item instanceof SequenceFlow
                && "approve".equals(((SequenceFlow) item).getSourceRef())
                && "end".equals(((SequenceFlow) item).getTargetRef())));
    }

    @Test
    public void testBuildBpmnModel_approveConfig() {
        // 准备参数
        BpmSimpleModelNodeVO approveNode = node("approve", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RATIO.getMethod()).setApproveRatio(60)
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1")
                .setTimeoutHandler(new BpmSimpleModelNodeVO.TimeoutHandler()
                        .setEnable(true).setType(BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType())
                        .setTimeDuration("PT1H").setMaxRemindCount(3));

        // 调用
        List<FlowElement> flowElements = new ApproveNodeFlowBuilder().convert(approveNode);
        // 断言
        UserTask userTask = (UserTask) flowElements.get(0);
        assertEquals("${ nrOfCompletedInstances/nrOfInstances >= 0.60}",
                userTask.getLoopCharacteristics().getCompletionCondition());
        assertEquals(BpmTaskCandidateStrategyEnum.USER.getStrategy(), BpmnModelUtils.parseCandidateStrategy(userTask));
        BoundaryEvent boundaryEvent = (BoundaryEvent) flowElements.get(1);
        TimerEventDefinition timer = (TimerEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        assertEquals("PT1H", timer.getTimeDuration());
        assertEquals("R3/PT1H", timer.getTimeCycle());
    }

    @Test
    public void testBuildBpmnModel_timeoutJump() {
        // 准备参数：审批超时 1 小时，自动跳转到 target 节点
        BpmSimpleModelNodeVO approveNode = node("approve", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setName("审批").setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod())
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1")
                .setTimeoutHandler(new BpmSimpleModelNodeVO.TimeoutHandler()
                        .setEnable(true).setType(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType())
                        .setTimeDuration("PT1H").setMaxRemindCount(3).setJumpNodeId("target"));
        approveNode.setChildNode(node("target", BpmSimpleModelNodeTypeEnum.APPROVE_NODE).setName("目标")
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod())
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1")
                .setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE)));

        // 调用
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel("process", "测试流程", approveNode);
        // 断言：超时边界事件记录执行动作、目标节点，只触发一次（提醒次数只对自动提醒生效）
        BoundaryEvent boundaryEvent = bpmnModel.getMainProcess().getFlowElements().stream()
                .filter(item -> item instanceof BoundaryEvent).map(item -> (BoundaryEvent) item)
                .findFirst().orElseThrow();
        assertEquals("approve", boundaryEvent.getAttachedToRef().getId());
        assertEquals(String.valueOf(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType()),
                BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_HANDLER_TYPE));
        assertEquals("target", BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_JUMP_NODE_ID));
        TimerEventDefinition timer = (TimerEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        assertEquals("PT1H", timer.getTimeDuration());
        assertNull(timer.getTimeCycle());
        // 断言：与部署后一致，从 XML 解析的模型中，可以通过审批节点解析出跳转的目标节点
        BpmnModel parsedModel = BpmnModelUtils.getBpmnModel(
                BpmnModelUtils.getBpmnXml(bpmnModel).getBytes(StandardCharsets.UTF_8));
        assertEquals("target", BpmnModelUtils.parseTimeoutJumpNodeId(parsedModel.getFlowElement("approve")));
        assertNull(BpmnModelUtils.parseTimeoutJumpNodeId(parsedModel.getFlowElement("target")));
    }

    @Test
    public void testIsValidTimeoutJump() {
        // 准备参数：发起人 -> A -> 条件分支（B1 -> B2；默认 C1）-> 并行分支（P1；P2 -> 嵌套条件分支 N1）-> 包容分支（I1）
        // -> 办理 T -> 自动通过 AUTO -> 结束
        BpmSimpleModelNodeVO end = node("end", BpmSimpleModelNodeTypeEnum.END_NODE);
        BpmSimpleModelNodeVO auto = node("auto", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()).setChildNode(end);
        BpmSimpleModelNodeVO transactor = node("t", BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType()).setChildNode(auto);
        BpmSimpleModelNodeVO inclusive = node("inclusive", BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE)
                .setConditionNodes(List.of(conditionNode("i-1", userApproveNode("i1")))).setChildNode(transactor);
        BpmSimpleModelNodeVO nested = node("nested", BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE)
                .setConditionNodes(List.of(conditionNode("n-1", userApproveNode("n1"))));
        BpmSimpleModelNodeVO parallel = node("parallel", BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE)
                .setConditionNodes(List.of(conditionNode("p-1", userApproveNode("p1")),
                        conditionNode("p-2", userApproveNode("p2").setChildNode(nested))))
                .setChildNode(inclusive);
        BpmSimpleModelNodeVO b1 = userApproveNode("b1").setChildNode(userApproveNode("b2"));
        BpmSimpleModelNodeVO branch = node("branch", BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE)
                .setConditionNodes(List.of(conditionNode("c-1", b1), conditionNode("c-2", userApproveNode("c1"))))
                .setChildNode(parallel);
        BpmSimpleModelNodeVO root = node("start-user", BpmSimpleModelNodeTypeEnum.START_USER_NODE)
                .setChildNode(userApproveNode("a").setChildNode(branch));

        // 断言：同一条件分支、不同条件分支、往回跳转，都有效
        assertTrue(SimpleModelUtils.isValidTimeoutJump(root, "b1", "b2"));
        assertTrue(SimpleModelUtils.isValidTimeoutJump(root, "b1", "c1"));
        assertTrue(SimpleModelUtils.isValidTimeoutJump(root, "c1", "a"));
        // 断言：跳转到自己、非人工审批的审批节点、不存在的节点，无效
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "b1"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", null));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "start-user"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "t"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "auto"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "not-exists"));
        // 断言：并行分支、包容分支内（包括嵌套在其中的条件分支）的节点，不能作为当前节点、目标节点
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "p1"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "n1"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "i1"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "p1", "a"));
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "i1", "a"));

        // 调用 + 断言：只校验开启自动跳转的节点
        b1.setTimeoutHandler(timeoutJumpHandler("c1"));
        findNode(root, "p2").setTimeoutHandler(timeoutJumpHandler("a").setEnable(false));
        findNode(root, "c1").setTimeoutHandler(new BpmSimpleModelNodeVO.TimeoutHandler().setEnable(true)
                .setType(BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType()).setTimeDuration("PT1H"));
        assertNull(SimpleModelUtils.findInvalidTimeoutJumpNode(root));
        // 调用 + 断言：并行分支内的节点开启自动跳转，无效
        findNode(root, "p1").setTimeoutHandler(timeoutJumpHandler("a"));
        assertEquals("p1", SimpleModelUtils.findInvalidTimeoutJumpNode(root).getId());

        // 断言：目标节点的审批人为【发起人自选】、【审批人自选】时，无效；当前节点为自选时，仍然有效
        findNode(root, "c1").setCandidateStrategy(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy());
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "b1", "c1"));
        assertTrue(SimpleModelUtils.isValidTimeoutJump(root, "c1", "a"));
        findNode(root, "a").setCandidateStrategy(BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy());
        assertFalse(SimpleModelUtils.isValidTimeoutJump(root, "c1", "a"));
        assertTrue(SimpleModelUtils.isValidTimeoutJump(root, "c1", "b2"));
        // 调用 + 断言：b1 跳转到【发起人自选】的 c1，无效
        assertEquals("b1", SimpleModelUtils.findInvalidTimeoutJumpNode(root).getId());
    }

    @Test
    public void testSimulateProcessAfterNodes() {
        // 准备参数：发起人 -> A -> 条件分支（金额大于 100：B1 -> B2；默认：C1）-> 并行分支（P1；P2）-> D -> 结束
        BpmSimpleModelNodeVO parallel = node("parallel", BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE)
                .setConditionNodes(List.of(conditionNode("p-1", userApproveNode("p1")),
                        conditionNode("p-2", userApproveNode("p2"))))
                .setChildNode(userApproveNode("d").setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE)));
        BpmSimpleModelNodeVO branch = node("branch", BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE)
                .setConditionNodes(List.of(
                        expressionConditionNode("c-1", "${amount > 100}", userApproveNode("b1").setChildNode(userApproveNode("b2"))),
                        expressionConditionNode("c-2", null, userApproveNode("c1"))))
                .setChildNode(parallel);
        BpmSimpleModelNodeVO root = node("start-user", BpmSimpleModelNodeTypeEnum.START_USER_NODE)
                .setChildNode(userApproveNode("a").setChildNode(branch));
        Map<String, Object> variables = Map.of("amount", 200);

        // 断言：从头预测时，按流程变量走 B1 分支
        assertEquals(List.of("start-user", "a", "b1", "b2", "p1", "p2", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcess(root, variables)));
        // 断言：跨条件分支跳转到 C1 后，只预测 C1 之后的节点，不包括按流程变量选择的 B1、B2 分支
        assertEquals(List.of("p1", "p2", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("c1"), variables)));
        // 断言：往回跳转到 A 后，之后的节点（包括并行分支）都需要预测，分支仍然按流程变量选择
        assertEquals(List.of("b1", "b2", "p1", "p2", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("a"), variables)));
        assertEquals(List.of("c1", "p1", "p2", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("a"), Map.of("amount", 50))));
        // 断言：并行分支内多个进行中的节点，分支聚合之后的节点去重，不包括进行中的节点
        assertEquals(List.of("d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("p1", "p2"), variables)));
        // 断言：进行中的节点不在模型中时，无法预测，返回空
        assertEquals(List.of(),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("not-exists"), variables)));
    }

    @Test
    public void testSimulateProcessAfterNodes_parallelBranchOrder() {
        // 准备参数：发起人 -> 并行分支（P1a -> P1b；P2a -> P2b）-> D -> 结束
        BpmSimpleModelNodeVO parallel = node("parallel", BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE)
                .setConditionNodes(List.of(
                        conditionNode("p-1", userApproveNode("p1a").setChildNode(userApproveNode("p1b"))),
                        conditionNode("p-2", userApproveNode("p2a").setChildNode(userApproveNode("p2b")))))
                .setChildNode(userApproveNode("d").setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE)));
        BpmSimpleModelNodeVO root = node("start-user", BpmSimpleModelNodeTypeEnum.START_USER_NODE).setChildNode(parallel);
        Map<String, Object> variables = Collections.emptyMap();

        // 断言：两个分支都有后续节点时，各分支的后续节点，都在分支聚合之后的 D、结束之前；与进行中节点的顺序无关
        assertEquals(List.of("p1b", "p2b", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("p1a", "p2a"), variables)));
        assertEquals(List.of("p1b", "p2b", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("p2a", "p1a"), variables)));
        // 断言：一个分支已经到最后一个节点时，另一个分支的后续节点，仍然在 D、结束之前
        assertEquals(List.of("p2b", "d", "end"),
                getNodeIds(SimpleModelUtils.simulateProcessAfterNodes(root, List.of("p1b", "p2a"), variables)));
    }

    @Test
    public void testDelayTimerNodeConvert_durationAndDate() {
        // 准备参数
        BpmSimpleModelNodeVO durationNode = node("delay-duration", BpmSimpleModelNodeTypeEnum.DELAY_TIMER_NODE)
                .setDelaySetting(new BpmSimpleModelNodeVO.DelaySetting()
                        .setDelayType(BpmDelayTimerTypeEnum.FIXED_TIME_DURATION.getType()).setDelayTime("PT2H"));
        BpmSimpleModelNodeVO dateNode = node("delay-date", BpmSimpleModelNodeTypeEnum.DELAY_TIMER_NODE)
                .setDelaySetting(new BpmSimpleModelNodeVO.DelaySetting()
                        .setDelayType(BpmDelayTimerTypeEnum.FIXED_DATE_TIME.getType())
                        .setDelayTime("2026-10-06T12:00:00"));

        // 调用
        BoundaryEvent durationEvent = (BoundaryEvent) new SimpleModelUtils.DelayTimerNodeConvert()
                .convertList(durationNode).get(1);
        BoundaryEvent dateEvent = (BoundaryEvent) new SimpleModelUtils.DelayTimerNodeConvert()
                .convertList(dateNode).get(1);
        // 断言
        TimerEventDefinition durationTimer = (TimerEventDefinition) durationEvent.getEventDefinitions().get(0);
        assertEquals("PT2H", durationTimer.getTimeDuration());
        assertNull(durationTimer.getTimeDate());
        TimerEventDefinition dateTimer = (TimerEventDefinition) dateEvent.getEventDefinitions().get(0);
        assertEquals("2026-10-06T12:00:00", dateTimer.getTimeDate());
        assertNull(dateTimer.getTimeDuration());
    }

    @Test
    public void testBuildBpmnModel_childProcessTimeout() {
        // 准备参数
        BpmSimpleModelNodeVO.ChildProcessSetting setting = new BpmSimpleModelNodeVO.ChildProcessSetting()
                .setCalledProcessDefinitionKey("child-process").setCalledProcessDefinitionName("子流程")
                .setAsync(false).setSkipStartUserNode(true)
                .setStartUserSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting()
                        .setType(BpmChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER.getType())
                        .setEmptyType(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER.getType()))
                .setTimeoutSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.TimeoutSetting()
                        .setEnable(true).setType(BpmDelayTimerTypeEnum.FIXED_DATE_TIME.getType())
                        .setTimeExpression("2026-10-06T12:00:00"));
        BpmSimpleModelNodeVO childNode = node("child", BpmSimpleModelNodeTypeEnum.CHILD_PROCESS)
                .setChildProcessSetting(setting).setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE));

        // 调用
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel("process", "测试流程", childNode);
        Process process = bpmnModel.getMainProcess();
        // 断言
        assertInstanceOf(CallActivity.class, process.getFlowElement("child"));
        BoundaryEvent boundaryEvent = process.getFlowElements().stream().filter(item -> item instanceof BoundaryEvent)
                .map(item -> (BoundaryEvent) item).findFirst().orElseThrow();
        TimerEventDefinition timer = (TimerEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        assertEquals("2026-10-06T12:00:00", timer.getTimeDate());
        assertNull(timer.getTimeDuration());
        assertEquals(String.valueOf(BpmBoundaryEventTypeEnum.CHILD_PROCESS_TIMEOUT.getType()),
                BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, BOUNDARY_EVENT_TYPE));
    }

    @Test
    public void testBuildBpmnModel_childProcessTimeoutDuration() {
        // 准备参数
        BpmSimpleModelNodeVO.ChildProcessSetting setting = new BpmSimpleModelNodeVO.ChildProcessSetting()
                .setCalledProcessDefinitionKey("child-process").setCalledProcessDefinitionName("子流程")
                .setAsync(false).setSkipStartUserNode(true)
                .setStartUserSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting()
                        .setType(BpmChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER.getType())
                        .setEmptyType(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER.getType()))
                .setTimeoutSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.TimeoutSetting()
                        .setEnable(true).setType(BpmDelayTimerTypeEnum.FIXED_TIME_DURATION.getType())
                        .setTimeExpression("PT30M"));
        BpmSimpleModelNodeVO childNode = node("child", BpmSimpleModelNodeTypeEnum.CHILD_PROCESS)
                .setChildProcessSetting(setting).setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE));

        // 调用
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel("process", "测试流程", childNode);
        Process process = bpmnModel.getMainProcess();
        // 断言：固定时长使用 timeDuration，且不会误设置 timeDate、timeCycle
        BoundaryEvent boundaryEvent = process.getFlowElements().stream().filter(item -> item instanceof BoundaryEvent)
                .map(item -> (BoundaryEvent) item).findFirst().orElseThrow();
        assertEquals("child", boundaryEvent.getAttachedToRef().getId());
        assertFalse(boundaryEvent.isCancelActivity());
        TimerEventDefinition timer = (TimerEventDefinition) boundaryEvent.getEventDefinitions().get(0);
        assertEquals("PT30M", timer.getTimeDuration());
        assertNull(timer.getTimeDate());
        assertNull(timer.getTimeCycle());
        // 断言：边界事件类型为子流程超时，而不是延迟器超时
        assertEquals(String.valueOf(BpmBoundaryEventTypeEnum.CHILD_PROCESS_TIMEOUT.getType()),
                BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, BOUNDARY_EVENT_TYPE));
    }

    @Test
    public void testBuildBpmnModel_conditionBranch() {
        // 准备参数
        BpmSimpleModelNodeVO branch = node("branch", BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE);
        BpmSimpleModelNodeVO condition = node("condition", BpmSimpleModelNodeTypeEnum.CONDITION_NODE)
                .setName("金额大于 10").setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                        .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                        .setConditionExpression("${amount > 10}").setDefaultFlow(false))
                .setChildNode(node("approve", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                        .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()));
        BpmSimpleModelNodeVO defaultCondition = node("default-condition", BpmSimpleModelNodeTypeEnum.CONDITION_NODE)
                .setName("默认").setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                        .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                        .setDefaultFlow(true))
                .setChildNode(node("reject", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                        .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType()));
        branch.setConditionNodes(Arrays.asList(condition, defaultCondition))
                .setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE));

        // 调用
        Process process = SimpleModelUtils.buildBpmnModel("process", "测试流程", branch).getMainProcess();
        // 断言
        ExclusiveGateway gateway = (ExclusiveGateway) process.getFlowElement("branch");
        assertEquals("default-condition", gateway.getDefaultFlow());
        SequenceFlow conditionFlow = (SequenceFlow) process.getFlowElement("condition");
        assertEquals("${amount > 10}", conditionFlow.getConditionExpression());
        assertEquals("approve", conditionFlow.getTargetRef());
    }

    @Test
    public void testTriggerNodeConvert_httpCallback() {
        // 准备参数
        BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting httpSetting =
                new BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting().setUrl("https://example.com/callback");
        BpmSimpleModelNodeVO node = node("trigger", BpmSimpleModelNodeTypeEnum.TRIGGER_NODE)
                .setTriggerSetting(new BpmSimpleModelNodeVO.TriggerSetting()
                        .setType(BpmTriggerTypeEnum.HTTP_CALLBACK.getType())
                        .setHttpRequestSetting(httpSetting));

        // 调用
        List<? extends FlowElement> flowElements = new SimpleModelUtils.TriggerNodeConvert().convertList(node);
        // 断言
        assertEquals(2, flowElements.size());
        assertInstanceOf(ReceiveTask.class, flowElements.get(0));
        assertInstanceOf(ServiceTask.class, flowElements.get(1));
        assertNotNull(httpSetting.getCallbackTaskDefineKey());
        assertEquals(httpSetting.getCallbackTaskDefineKey(), flowElements.get(0).getId());
    }

    @Test
    public void testBuildConditionExpression_rule() {
        // 准备参数
        BpmSimpleModelNodeVO.ConditionRule numberRule = new BpmSimpleModelNodeVO.ConditionRule()
                .setOpCode(BpmConditionOpCodeEnum.GT.getCode()).setLeftSide("amount").setRightSide("10");
        BpmSimpleModelNodeVO.ConditionRule stringRule = new BpmSimpleModelNodeVO.ConditionRule()
                .setOpCode(BpmConditionOpCodeEnum.EQ.getCode()).setLeftSide("status").setRightSide("PASS");
        BpmSimpleModelNodeVO.Condition condition = new BpmSimpleModelNodeVO.Condition()
                .setAnd(true).setRules(Arrays.asList(numberRule, stringRule));
        BpmSimpleModelNodeVO.ConditionGroups groups = new BpmSimpleModelNodeVO.ConditionGroups()
                .setAnd(false).setConditions(Collections.singletonList(condition));
        BpmSimpleModelNodeVO.ConditionSetting setting = new BpmSimpleModelNodeVO.ConditionSetting()
                .setConditionType(BpmSimpleModeConditionTypeEnum.RULE.getType()).setConditionGroups(groups);

        // 调用
        String expression = SimpleModelUtils.buildConditionExpression(setting);
        // 断言
        assertEquals("${( var:getOrDefault(amount, null) > 10  &&  var:getOrDefault(status, null) == \"PASS\" )}",
                expression);
    }

    @Test
    public void testBuildConditionExpression_expressionAndEmptyRule() {
        // 准备参数
        BpmSimpleModelNodeVO.ConditionSetting expressionSetting = new BpmSimpleModelNodeVO.ConditionSetting()
                .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                .setConditionExpression("${amount > 1}");
        BpmSimpleModelNodeVO.ConditionSetting emptyRuleSetting = new BpmSimpleModelNodeVO.ConditionSetting()
                .setConditionType(BpmSimpleModeConditionTypeEnum.RULE.getType())
                .setConditionGroups(new BpmSimpleModelNodeVO.ConditionGroups().setAnd(true)
                        .setConditions(Collections.emptyList()));

        // 调用，并断言
        assertEquals("${amount > 1}", SimpleModelUtils.buildConditionExpression(expressionSetting));
        assertNull(SimpleModelUtils.buildConditionExpression(emptyRuleSetting));
    }

    @Test
    public void testSimulateProcess_conditionBranches() {
        // 准备参数
        BpmSimpleModelNodeVO branch = node("branch", BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE);
        BpmSimpleModelNodeVO matched = node("matched", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType())
                .setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                        .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                        .setConditionExpression("${amount > 10}").setDefaultFlow(false));
        BpmSimpleModelNodeVO defaultNode = node("default", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())
                .setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                        .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                        .setDefaultFlow(true));
        matched.setChildNode(node("matched-end", BpmSimpleModelNodeTypeEnum.END_NODE));
        defaultNode.setChildNode(node("default-end", BpmSimpleModelNodeTypeEnum.END_NODE));
        branch.setConditionNodes(Arrays.asList(
                node("condition-1", BpmSimpleModelNodeTypeEnum.CONDITION_NODE).setConditionSetting(matched.getConditionSetting())
                        .setChildNode(matched),
                node("condition-2", BpmSimpleModelNodeTypeEnum.CONDITION_NODE).setConditionSetting(defaultNode.getConditionSetting())
                        .setChildNode(defaultNode)));

        // 调用
        List<BpmSimpleModelNodeVO> result = SimpleModelUtils.simulateProcess(branch,
                Collections.singletonMap("amount", 20));
        // 断言
        assertEquals(Arrays.asList("matched", "matched-end"), result.stream()
                .map(BpmSimpleModelNodeVO::getId).toList());
    }

    @Test
    public void testSimulateProcess_inclusiveAndParallelBranches() {
        // 准备参数
        BpmSimpleModelNodeVO inclusive = node("inclusive", BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE);
        BpmSimpleModelNodeVO first = node("first", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveType(BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType());
        BpmSimpleModelNodeVO second = node("second", BpmSimpleModelNodeTypeEnum.COPY_NODE);
        first.setChildNode(node("first-end", BpmSimpleModelNodeTypeEnum.END_NODE));
        second.setChildNode(node("second-end", BpmSimpleModelNodeTypeEnum.END_NODE));
        inclusive.setConditionNodes(Arrays.asList(
                node("condition-1", BpmSimpleModelNodeTypeEnum.CONDITION_NODE)
                        .setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                                .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                                .setConditionExpression("${left}").setDefaultFlow(false)).setChildNode(first),
                node("condition-2", BpmSimpleModelNodeTypeEnum.CONDITION_NODE)
                        .setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                                .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                                .setConditionExpression("${right}").setDefaultFlow(false)).setChildNode(second)));
        Map<String, Object> variables = new HashMap<>();
        variables.put("left", true);
        variables.put("right", true);

        // 调用
        List<BpmSimpleModelNodeVO> result = SimpleModelUtils.simulateProcess(inclusive, variables);
        // 断言
        assertEquals(Arrays.asList("first", "first-end", "second", "second-end"), result.stream()
                .map(BpmSimpleModelNodeVO::getId).toList());
    }

    @Test
    public void testNodeHelpers() {
        // 准备参数
        BpmSimpleModelNodeVO sequential = node("approve", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setApproveMethod(BpmUserTaskApproveMethodEnum.SEQUENTIAL.getMethod());
        BpmSimpleModelNodeVO skipNode = node("skip", BpmSimpleModelNodeTypeEnum.APPROVE_NODE)
                .setSkipExpression("${skip}");

        // 调用，并断言
        assertTrue(SimpleModelUtils.isValidNode(sequential));
        assertFalse(SimpleModelUtils.isValidNode(new BpmSimpleModelNodeVO()));
        assertTrue(SimpleModelUtils.isSequentialApproveNode(sequential));
        assertFalse(SimpleModelUtils.isSequentialApproveNode(node("other", BpmSimpleModelNodeTypeEnum.COPY_NODE)
                .setApproveMethod(BpmUserTaskApproveMethodEnum.SEQUENTIAL.getMethod())));
        assertTrue(SimpleModelUtils.isSkipNode(skipNode, Collections.singletonMap("skip", true)));
        assertFalse(SimpleModelUtils.isSkipNode(node("without-skip", BpmSimpleModelNodeTypeEnum.APPROVE_NODE),
                Collections.emptyMap()));
    }

    private static BpmSimpleModelNodeVO node(String id, BpmSimpleModelNodeTypeEnum type) {
        return new BpmSimpleModelNodeVO().setId(id).setType(type.getType());
    }

    private static BpmSimpleModelNodeVO userApproveNode(String id) {
        return node(id, BpmSimpleModelNodeTypeEnum.APPROVE_NODE).setName(id)
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType());
    }

    private static BpmSimpleModelNodeVO conditionNode(String id, BpmSimpleModelNodeVO childNode) {
        return node(id, BpmSimpleModelNodeTypeEnum.CONDITION_NODE).setChildNode(childNode);
    }

    private static BpmSimpleModelNodeVO expressionConditionNode(String id, String expression, BpmSimpleModelNodeVO childNode) {
        return conditionNode(id, childNode).setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                .setConditionExpression(expression).setDefaultFlow(expression == null));
    }

    private static List<String> getNodeIds(List<BpmSimpleModelNodeVO> nodes) {
        return nodes.stream().map(BpmSimpleModelNodeVO::getId).toList();
    }

    private static BpmSimpleModelNodeVO.TimeoutHandler timeoutJumpHandler(String jumpNodeId) {
        return new BpmSimpleModelNodeVO.TimeoutHandler().setEnable(true)
                .setType(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType()).setTimeDuration("PT1H")
                .setJumpNodeId(jumpNodeId);
    }

    /**
     * 查找节点，包括分支内的节点
     */
    private static BpmSimpleModelNodeVO findNode(BpmSimpleModelNodeVO node, String id) {
        if (node == null) {
            return null;
        }
        if (id.equals(node.getId())) {
            return node;
        }
        if (node.getConditionNodes() != null) {
            for (BpmSimpleModelNodeVO conditionNode : node.getConditionNodes()) {
                BpmSimpleModelNodeVO result = findNode(conditionNode, id);
                if (result != null) {
                    return result;
                }
            }
        }
        return findNode(node.getChildNode(), id);
    }

    /**
     * 通过受保护转换器验证审批节点配置，不暴露生产实现细节。
     */
    private static class ApproveNodeFlowBuilder {
        List<FlowElement> convert(BpmSimpleModelNodeVO node) {
            BpmnModel model = SimpleModelUtils.buildBpmnModel("test", "test",
                    node.setChildNode(node("end", BpmSimpleModelNodeTypeEnum.END_NODE)));
            Process process = model.getMainProcess();
            return Arrays.asList(process.getFlowElement("approve"), process.getFlowElements().stream()
                    .filter(item -> item instanceof BoundaryEvent).map(item -> (BoundaryEvent) item)
                    .findFirst().orElseThrow());
        }
    }
}
