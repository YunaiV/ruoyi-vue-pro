package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowReplyDTO;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotShadowPassthroughCodec;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 设备影子网关透传序列化单元测试
 */
public class IotDeviceShadowPassthroughTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceMessageServiceImpl deviceMessageService;

    @Test
    public void testSerializeDeviceMessage_passthrough_noEnvelope() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("method", "get_reply");
        payload.put("version", 2L);
        payload.put("timestamp", 1469564576L);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.SHADOW_GET.getMethod(), payload);

        byte[] bytes = deviceMessageService.serializeDeviceMessage(message, IotSerializeTypeEnum.JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = JsonUtils.parseObject(bytes, Map.class);
        assertEquals("get_reply", result.get("method"));
        assertEquals(2, ((Number) result.get("version")).intValue());
        assertNull(result.get("id"));
        assertNull(result.get("code"));
        assertNull(result.get("requestId"));
    }

    @Test
    public void testDeserializeUpstreamShadow_fromTopicMethod() {
        // 模拟上行透传：原始 body 不含 thing.shadow.update 封套 method
        Map<String, Object> body = new HashMap<>();
        body.put("method", "get");
        IotDeviceMessage message = IotDeviceMessage.requestOf("req-1",
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), body);

        assertEquals(IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), message.getMethod());
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
        assertNull(result.get("id"));
    }

    @Test
    public void testPassthroughCodec_jsonSerializeType() {
        Map<String, Object> params = Map.of("method", "get");
        byte[] bytes = IotShadowPassthroughCodec.encode(params, IotSerializeTypeEnum.JSON);
        Map<String, Object> decoded = IotShadowPassthroughCodec.decode(bytes, IotSerializeTypeEnum.JSON);
        assertNotNull(decoded);
        assertEquals("get", decoded.get("method"));
    }

    @Test
    public void testPassthroughCodec_nullSerializeTypeAsJson() {
        Map<String, Object> params = Map.of("method", "get");
        byte[] bytes = IotShadowPassthroughCodec.encode(params, null);
        Map<String, Object> decoded = IotShadowPassthroughCodec.decode(bytes, null);
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
    public void testPassthroughCodec_binaryDrop() {
        Map<String, Object> params = Map.of("method", "update");
        assertNull(IotShadowPassthroughCodec.encode(params, IotSerializeTypeEnum.BINARY));
        assertNull(IotShadowPassthroughCodec.decode(new byte[]{0x01}, IotSerializeTypeEnum.BINARY));
    }

}
