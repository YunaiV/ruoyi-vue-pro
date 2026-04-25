package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiModelUsageRepository;
import cn.iocoder.yudao.module.deepay.service.gateway.GatewayRequest;
import cn.iocoder.yudao.module.deepay.service.gateway.ModelGatewayException;
import cn.iocoder.yudao.module.deepay.service.gateway.ModelGatewayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ModelGatewayService 单元测试（不真实调用外部 API）。
 */
@ExtendWith(MockitoExtension.class)
class ModelGatewayServiceTest {

    @Mock
    private AiModelUsageRepository usageRepo;

    @InjectMocks
    private ModelGatewayService gatewayService;

    @Test
    void chat_noApiConfigured_throwsGatewayException() {
        // 不配置 API URL/Key，应抛出异常
        ReflectionTestUtils.setField(gatewayService, "primaryModel",   "deepseek-chat");
        ReflectionTestUtils.setField(gatewayService, "primaryApiUrl",   "");
        ReflectionTestUtils.setField(gatewayService, "primaryApiKey",   "");
        ReflectionTestUtils.setField(gatewayService, "fallbackApiUrl",  "");
        ReflectionTestUtils.setField(gatewayService, "fallbackApiKey",  "");
        ReflectionTestUtils.setField(gatewayService, "timeoutSeconds",  5);
        ReflectionTestUtils.setField(gatewayService, "maxRetries",      0);
        gatewayService.init();

        GatewayRequest req = GatewayRequest.builder()
                .tenantId(1L)
                .messages(List.of(Map.of("role", "user", "content", "hello")))
                .build();

        assertThatThrownBy(() -> gatewayService.chat(req))
                .isInstanceOf(ModelGatewayException.class);
    }

}
