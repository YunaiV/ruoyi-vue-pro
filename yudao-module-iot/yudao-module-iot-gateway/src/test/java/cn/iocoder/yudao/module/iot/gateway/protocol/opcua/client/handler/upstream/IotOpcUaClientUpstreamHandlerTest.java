package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientUpstreamHandler} 的单元测试：按字节数拆分、属性上报、取值过滤
 * <p>
 * 按点数拆分由 {@link #testHandlePropertyPost()} 和集成测试覆盖
 *
 * @author 芋道源码
 */
public class IotOpcUaClientUpstreamHandlerTest {

    private Vertx vertx;
    private IotOpcUaClientSession session;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        session = new IotOpcUaClientSession("test", vertx.getOrCreateContext());
    }

    @AfterEach
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void testSplitProperties_byBytes() {
        // {"a":"xxx...","b":"xxx..."}：每个属性 516 字节，两个属性 2 + 516 + 1 + 516 = 1035 字节，超过 1024
        IotOpcUaClientUpstreamHandler handler = new IotOpcUaClientUpstreamHandler(null, "s1", 100, 1024);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("a", "x".repeat(510));
        properties.put("b", "x".repeat(510));
        properties.put("c", 1);
        // 单个属性超过上限：丢弃
        properties.put("huge", "x".repeat(2000));

        List<Map<String, Object>> result = handler.splitProperties(properties);

        assertEquals(2, result.size());
        assertEquals(List.of("a"), List.copyOf(result.get(0).keySet()));
        assertEquals(List.of("b", "c"), List.copyOf(result.get(1).keySet()));
    }

    @Test
    public void testHandlePropertyPost() {
        IotDeviceMessageService messageService = mock(IotDeviceMessageService.class);
        IotOpcUaClientUpstreamHandler handler = new IotOpcUaClientUpstreamHandler(messageService, "s1", 1, 1024);
        IotOpcUaDeviceConfigRespDTO config = new IotOpcUaDeviceConfigRespDTO();
        config.setDeviceId(1L);
        config.setProductKey("pk");
        config.setDeviceName("dn");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("a", 1);
        properties.put("b", 2);

        handler.handlePropertyPost(config, properties);

        // 断言：拆分为两条属性上报，走离线暂存（带协议）
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(messageService, times(2)).sendDeviceMessage(captor.capture(), eq("pk"), eq("dn"), eq("s1"),
                eq(IotProtocolTypeEnum.OPC_UA_CLIENT));
        assertEquals("thing.property.post", captor.getAllValues().get(0).getMethod());
        assertEquals(Map.of("a", 1), captor.getAllValues().get(0).getParams());
        assertEquals(Map.of("b", 2), captor.getAllValues().get(1).getParams());
    }

    @Test
    public void testCollectValue() {
        IotOpcUaClientUpstreamHandler handler = new IotOpcUaClientUpstreamHandler(mock(IotDeviceMessageService.class),
                "s1", 100, 1024);
        Map<String, Object> target = new LinkedHashMap<>();

        handler.collectValue(session, 1L, "good", new DataValue(new Variant(1.5D)), target);
        handler.collectValue(session, 1L, "bad", new DataValue(new StatusCode(StatusCodes.Bad_SensorFailure)), target);
        handler.collectValue(session, 1L, "uncertain", new DataValue(new Variant(2),
                new StatusCode(StatusCodes.Uncertain_LastUsableValue)), target);
        handler.collectValue(session, 1L, "nan", new DataValue(new Variant(Double.NaN)), target);

        // 断言：只保留 Good 且可转换的值；跳过的每代只告警一次
        assertEquals(Map.of("good", 1.5D), target);
        assertFalse(session.warnOnce("type:1:nan"));
    }

}
