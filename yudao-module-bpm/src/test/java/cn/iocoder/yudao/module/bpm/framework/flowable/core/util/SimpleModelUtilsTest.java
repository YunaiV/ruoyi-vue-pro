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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.BOUNDARY_EVENT_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_EVENT_NODE_ID;
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
