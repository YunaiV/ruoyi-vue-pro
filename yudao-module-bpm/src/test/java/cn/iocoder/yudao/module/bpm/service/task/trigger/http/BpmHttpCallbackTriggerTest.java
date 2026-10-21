package cn.iocoder.yudao.module.bpm.service.task.trigger.http;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmHttpCallbackTrigger} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmHttpCallbackTriggerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmHttpCallbackTrigger httpCallbackTrigger;

    @Mock
    private BpmProcessInstanceService processInstanceService;

    @Test
    @SuppressWarnings("unchecked")
    public void testExecute_bodyNull() {
        // mock 方法
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        // 准备参数：回调配置未配置 body
        BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting setting =
                new BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting();
        setting.setUrl("http://127.0.0.1/callback");
        setting.setCallbackTaskDefineKey("task1");

        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            // 调用
            httpCallbackTrigger.execute("process-instance-1", JsonUtils.toJsonString(setting));

            // 断言：body 为空时不会空指针，并追加回调的 taskDefineKey
            ArgumentCaptor<List<BpmSimpleModelNodeVO.HttpRequestParam>> bodyCaptor = ArgumentCaptor.forClass(List.class);
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(same(processInstance),
                    eq("http://127.0.0.1/callback"), isNull(), bodyCaptor.capture(), eq(false), isNull()));
            List<BpmSimpleModelNodeVO.HttpRequestParam> body = bodyCaptor.getValue();
            assertEquals(1, body.size());
            assertEquals("taskDefineKey", body.get(0).getKey());
            assertEquals("task1", body.get(0).getValue());
        }
    }

}
