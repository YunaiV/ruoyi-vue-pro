package cn.iocoder.yudao.module.bpm.framework.flowable.core.trigger.form;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.FormTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmFormDeleteTrigger} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmFormDeleteTriggerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmFormDeleteTrigger formDeleteTrigger;

    @Mock
    private BpmProcessInstanceQueryService processInstanceQueryService;
    @Mock
    private BpmProcessInstanceUpdateService processInstanceUpdateService;

    @Test
    public void testExecute_conditionFilter() {
        // mock 方法
        Map<String, Object> processVariables = Map.of("day", 3);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getProcessVariables()).thenReturn(processVariables);
        when(processInstanceQueryService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        // 准备参数：无条件、条件满足、条件不满足
        List<FormTriggerSetting> settings = List.of(
                buildDeleteSetting(null, Set.of("a")),
                buildDeleteSetting("${match}", Set.of("b")),
                buildDeleteSetting("${notMatch}", Set.of("c")));

        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class)) {
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.evalConditionExpress(processVariables, "${match}"))
                    .thenReturn(true);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.evalConditionExpress(processVariables, "${notMatch}"))
                    .thenReturn(false);

            // 调用
            formDeleteTrigger.execute("process-instance-1", JsonUtils.toJsonString(settings));
        }
        // 断言：合并删除无条件、条件满足的表单字段
        verify(processInstanceUpdateService).removeProcessInstanceVariables("process-instance-1", Set.of("a", "b"));
    }

    @Test
    public void testExecute_conditionNotMatched() {
        // mock 方法
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstanceQueryService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        // 准备参数：条件不满足
        List<FormTriggerSetting> settings = List.of(buildDeleteSetting("${notMatch}", Set.of("c")));

        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class)) {
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.evalConditionExpress(any(), eq("${notMatch}")))
                    .thenReturn(false);

            // 调用
            formDeleteTrigger.execute("process-instance-1", JsonUtils.toJsonString(settings));
        }
        // 断言：没有需要删除的字段时，不删除流程变量
        verifyNoInteractions(processInstanceUpdateService);
    }

    private static FormTriggerSetting buildDeleteSetting(String conditionExpression, Set<String> deleteFields) {
        FormTriggerSetting setting = new FormTriggerSetting();
        if (conditionExpression != null) {
            setting.setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType());
            setting.setConditionExpression(conditionExpression);
        }
        setting.setDeleteFields(deleteFields);
        return setting;
    }

}
