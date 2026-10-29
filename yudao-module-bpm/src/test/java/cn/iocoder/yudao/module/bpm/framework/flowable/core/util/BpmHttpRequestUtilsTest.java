package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.framework.flowable.config.BpmHttpRequestProperties;
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

import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_HTTP_CALL_ERROR;
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

    @Test
    public void testExecuteBpmHttpRequest_disabled() {
        // 准备参数：默认关闭，不做任何校验，与原有行为一致
        BpmHttpRequestProperties properties = new BpmHttpRequestProperties();
        properties.setDenyHosts(Set.of("169.254.169.254"));

        // 调用，并断言：仍然发起请求
        executeBpmHttpRequestSuccess(properties, "http://169.254.169.254/latest");
    }

    @Test
    public void testExecuteBpmHttpRequest_allowHostMatch() {
        // 准备参数：域名、IP 精确匹配，忽略大小写
        BpmHttpRequestProperties properties = buildProperties(Set.of("ERP.example.internal", "10.0.0.8", "::1"), Set.of());

        // 调用，并断言：发起请求
        executeBpmHttpRequestSuccess(properties, "https://erp.EXAMPLE.internal/api",
                "http://10.0.0.8:8080/callback", "http://[::1]:48080/callback");
    }

    @Test
    public void testExecuteBpmHttpRequest_denyHostFirst() {
        // 准备参数：同时在黑名单、白名单中，黑名单优先
        BpmHttpRequestProperties properties = buildProperties(Set.of("10.0.0.8"), Set.of(" 10.0.0.8 "));

        // 调用，并断言：拒绝请求
        executeBpmHttpRequestFail(properties, "http://10.0.0.8/callback");
    }

    @Test
    public void testExecuteBpmHttpRequest_notInAllowHosts() {
        // 准备参数：白名单非空时，只允许列表中的域名或 IP，不做后缀、包含匹配
        BpmHttpRequestProperties properties = buildProperties(Set.of("erp.example.internal"), Set.of());

        // 调用，并断言：拒绝请求
        executeBpmHttpRequestFail(properties, "http://a.erp.example.internal/callback",
                "http://erp.example.internal.evil.com/callback", "http://10.0.0.9/callback");
    }

    @Test
    public void testExecuteBpmHttpRequest_emptyHosts() {
        // 准备参数：开启，但黑白名单都为空时，不额外限制
        BpmHttpRequestProperties properties = buildProperties(Set.of(), Set.of());

        // 调用，并断言：发起请求
        executeBpmHttpRequestSuccess(properties, "http://169.254.169.254/latest");
    }

    @Test
    public void testExecuteBpmHttpRequest_denyHostsOnly() {
        // 准备参数：只配置黑名单时，命中黑名单拒绝
        BpmHttpRequestProperties properties = buildProperties(Set.of(), Set.of("169.254.169.254"));

        // 调用，并断言：拒绝请求
        executeBpmHttpRequestFail(properties, "http://169.254.169.254/latest");
    }

    @Test
    public void testExecuteBpmHttpRequest_invalidUrl() {
        // 准备参数：开启后，只允许带域名或 IP 的 http、https 地址
        BpmHttpRequestProperties properties = buildProperties(Set.of(), Set.of());

        // 调用，并断言：拒绝请求
        executeBpmHttpRequestFail(properties, "ftp://10.0.0.8/callback", "file:///etc/passwd",
                "//10.0.0.8/callback", "callback", "http:///callback", "", null);
    }

    private static BpmHttpRequestProperties buildProperties(Set<String> allowHosts, Set<String> denyHosts) {
        BpmHttpRequestProperties properties = new BpmHttpRequestProperties();
        properties.setEnabled(true);
        properties.setAllowHosts(allowHosts);
        properties.setDenyHosts(denyHosts);
        return properties;
    }

    private void executeBpmHttpRequestSuccess(BpmHttpRequestProperties properties, String... urls) {
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmHttpRequestProperties.class)).thenReturn(properties);
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmProcessInstanceService.class))
                    .thenReturn(processInstanceService);
            springUtilMockedStatic.when(() -> SpringUtil.getBean(RestTemplate.class)).thenReturn(restTemplate);

            for (String url : urls) {
                // 调用
                BpmHttpRequestUtils.executeBpmHttpRequest(processInstance, url, null, null, false, null);
                // 断言：使用原有 RestTemplate 发起请求
                verify(restTemplate).exchange(eq(url), any(), any(), eq(String.class));
            }
        }
    }

    private void executeBpmHttpRequestFail(BpmHttpRequestProperties properties, String... urls) {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmHttpRequestProperties.class)).thenReturn(properties);

            for (String url : urls) {
                // 调用，并断言
                assertServiceException(() -> BpmHttpRequestUtils.executeBpmHttpRequest(processInstance, url,
                        null, null, false, null), PROCESS_INSTANCE_HTTP_CALL_ERROR);
            }
        }
        // 断言：未发起请求
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }

}
