package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowReplyDTO;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 设备影子网关透传序列化单元测试
 */
public class IotDeviceShadowPassthroughTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceMessageServiceImpl deviceMessageService;

    @Mock
    private IotDeviceService deviceService;

    @Spy
    private IotMessageSerializerManager messageSerializerManager = new IotMessageSerializerManager();

    @BeforeEach
    public void setUpManager() {
        // Spy 需手动触发构造逻辑
        if (messageSerializerManager == null) {
            messageSerializerManager = new IotMessageSerializerManager();
        }
    }

    @Test
    public void testSerializeDeviceMessage_passthrough_flatReply() {
        IotDeviceShadowReplyDTO reply = IotDeviceShadowReplyDTO.getReplyOf(new HashMap<>(), new HashMap<>(), 2L, 1469564576L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.SHADOW_GET.getMethod(), reply);

        byte[] bytes = deviceMessageService.serializeDeviceMessage(message, IotSerializeTypeEnum.JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = JsonUtils.parseObject(bytes, Map.class);
        assertEquals("get_reply", result.get("method"));
        assertEquals(0, ((Number) result.get("code")).intValue());
        assertNull(result.get("payload"));
        assertNull(result.get("id"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, ((Number) data.get("version")).intValue());
    }

    @Test
    public void testDeserializePassthroughMessage_fromTopicMethod() {
        IotDeviceRespDTO device = new IotDeviceRespDTO().setId(1L).setSerializeType(IotSerializeTypeEnum.JSON.getType());
        when(deviceService.getDeviceFromCache(eq("pk"), eq("dn"))).thenReturn(device);

        byte[] payload = JsonUtils.toJsonByte(Map.of("method", "get", "id", "req-1"));
        IotDeviceMessage message = deviceMessageService.deserializePassthroughMessage(payload,
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), "pk", "dn");

        assertNotNull(message);
        assertEquals(IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), message.getMethod());
        assertEquals("req-1", message.getRequestId());
        assertEquals("get", ((Map<?, ?>) message.getParams()).get("method"));
    }

    @Test
    public void testSerializeUpdateReply_passthrough() {
        IotDeviceShadowReplyDTO reply = IotDeviceShadowReplyDTO.updateReplyOf(3L, 1469564576L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.SHADOW_GET.getMethod(), reply);

        byte[] bytes = deviceMessageService.serializeDeviceMessage(message, IotSerializeTypeEnum.JSON);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = JsonUtils.parseObject(bytes, Map.class);
        assertEquals("update_reply", result.get("method"));
        assertEquals(0, ((Number) result.get("code")).intValue());
        assertNull(result.get("id"));
    }

    @Test
    public void testJsonSerializer_encodeRawDecodeRaw() {
        IotJsonSerializer serializer = new IotJsonSerializer();
        Map<String, Object> params = Map.of("method", "get");
        byte[] bytes = serializer.encodeRaw(params);
        Map<String, Object> decoded = serializer.decodeRaw(bytes);
        assertNotNull(decoded);
        assertEquals("get", decoded.get("method"));
    }

    @Test
    public void testSerializeDeviceMessage_passthrough_binaryReturnsNull() {
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.SHADOW_GET.getMethod(), Map.of("method", "get_reply"));
        assertNull(deviceMessageService.serializeDeviceMessage(message, IotSerializeTypeEnum.BINARY));
    }

    @Test
    public void testBinarySerializer_rawNotSupported() {
        cn.iocoder.yudao.module.iot.gateway.serialize.binary.IotBinarySerializer serializer =
                new cn.iocoder.yudao.module.iot.gateway.serialize.binary.IotBinarySerializer();
        assertThrows(UnsupportedOperationException.class, () -> serializer.encodeRaw(Map.of("method", "update")));
    }

    @Test
    public void testDeserializePassthroughMessage_numericId() {
        IotDeviceRespDTO device = new IotDeviceRespDTO().setId(1L).setSerializeType(IotSerializeTypeEnum.JSON.getType());
        when(deviceService.getDeviceFromCache(eq("pk"), eq("dn"))).thenReturn(device);

        byte[] payload = JsonUtils.toJsonByte(Map.of("method", "get", "id", 123));
        IotDeviceMessage message = deviceMessageService.deserializePassthroughMessage(payload,
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), "pk", "dn");

        assertNotNull(message);
        assertEquals("123", message.getRequestId());
    }

    @Test
    public void testDeserializePassthroughMessage_invalidJsonStillDelivered() {
        IotDeviceRespDTO device = new IotDeviceRespDTO().setId(1L).setSerializeType(IotSerializeTypeEnum.JSON.getType());
        when(deviceService.getDeviceFromCache(eq("pk"), eq("dn"))).thenReturn(device);

        IotDeviceMessage message = deviceMessageService.deserializePassthroughMessage("{invalid".getBytes(),
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), "pk", "dn");

        assertNotNull(message);
        assertNull(message.getParams());
    }

    @Test
    public void testErrorOf_getMethodUsesGetReply() {
        IotDeviceShadowReplyDTO reply = IotDeviceShadowReplyDTO.errorOf("get", 1L, 400, "JSON 格式错误", 1000L);
        assertEquals("get_reply", reply.getMethod());
        assertEquals(400, reply.getCode());
    }

}
