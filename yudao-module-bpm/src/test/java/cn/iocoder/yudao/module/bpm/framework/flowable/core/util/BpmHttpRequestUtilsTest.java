package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmHttpRequestUtils} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmHttpRequestUtilsTest extends BaseMockitoUnitTest {

    @Mock
    private BpmProcessInstanceService processInstanceService;
    @Mock
    private ProcessInstance processInstance;
    @Mock
    private RestTemplate restTemplate;

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testExecuteBpmHttpRequest_statusEventTenantFromProcessInstance() {
        // 准备参数：事务完成后的回调可能已经没有租户上下文，但流程实例仍带有租户编号
        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("process-instance");
        when(processInstanceService.getProcessInstance("process-instance")).thenReturn(processInstance);
        when(processInstance.getTenantId()).thenReturn("tenant-1");
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));

        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmProcessInstanceService.class))
                    .thenReturn(processInstanceService);
            springUtilMockedStatic.when(() -> SpringUtil.getBean(RestTemplate.class)).thenReturn(restTemplate);

            // 调用
            BpmHttpRequestUtils.executeBpmHttpRequest(event, "http://callback");
        }
        // 断言：请求头使用流程实例租户，而不是已清空的上下文租户
        ArgumentCaptor<HttpEntity<Object>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("http://callback"), any(), requestCaptor.capture(), eq(String.class));
        assertEquals("tenant-1", requestCaptor.getValue().getHeaders().getFirst(HEADER_TENANT_ID));
    }

}
