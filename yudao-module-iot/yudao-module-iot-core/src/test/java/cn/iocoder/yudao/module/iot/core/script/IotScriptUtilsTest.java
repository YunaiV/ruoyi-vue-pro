package cn.iocoder.yudao.module.iot.core.script;

import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotScriptUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotScriptUtilsTest {

    @Test
    public void testValidateDecode_success() throws IotScriptException {
        // 调用：一个标准消息对象
        IotDeviceMessage message = IotScriptUtils.validateDecode(
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":25}}", null);
        // 断言
        assertEquals("thing.property.post", message.getMethod());
        assertEquals(25, ((Map<?, ?>) message.getParams()).get("t"));
        assertNull(message.getCode());
        assertNull(message.getRaw());
        // 调用 + 断言：事件
        IotDeviceMessage event = IotScriptUtils.validateDecode(
                "{\"method\":\"thing.event.post\",\"params\":{\"identifier\":\"alarm\",\"value\":{}}}", null);
        assertEquals("thing.event.post", event.getMethod());
    }

    @Test
    public void testValidateDecode_null() throws IotScriptException {
        // 调用 + 断言：decode 未定义或返回 null → 没有业务消息
        assertNull(IotScriptUtils.validateDecode(null, null));
    }

    @Test
    public void testValidateDecode_invalid() {
        // 不是对象、旧的 messages / ack 包装
        assertOutputInvalid("[]");
        assertOutputInvalid("{\"messages\":[{\"method\":\"thing.property.post\",\"params\":{}}]}");
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"ack\":[1]}");
        // 身份、路由、可信字段和未知键不允许：脚本不能伪造 raw、设备、租户、编号
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"deviceId\":1}");
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"tenantId\":1}");
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"id\":\"x\"}");
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"raw\":\"AQI=\"}");
        assertOutputInvalid("{\"method\":\"thing.property.post\",\"params\":{},\"unknown\":true}");
        // 不支持的方法；事件缺 identifier；回执缺 requestId
        assertOutputInvalid("{\"method\":\"thing.raw.post\",\"params\":{}}");
        assertOutputInvalid("{\"method\":\"thing.event.post\",\"params\":{}}");
        assertOutputInvalid("{\"method\":\"thing.property.set\",\"code\":0}");
    }

    @Test
    public void testValidateDecode_transactionReply() throws IotScriptException {
        // 准备参数
        IotDeviceMessage request = IotDeviceMessage.requestOf("req-1", "thing.property.set", null);

        // 调用 + 断言：方法、requestId 一致的回执
        IotDeviceMessage reply = IotScriptUtils.validateDecode(
                "{\"method\":\"thing.property.set\",\"requestId\":\"req-1\",\"code\":0}", request);
        assertEquals(0, reply.getCode());
        // 调用 + 断言：requestId 不一致、不是回执、没有输出
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateDecode(
                "{\"method\":\"thing.property.set\",\"requestId\":\"req-2\",\"code\":0}", request));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateDecode(
                "{\"method\":\"thing.property.post\",\"params\":{}}", request));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateDecode(null, request));
    }

    @Test
    public void testValidateEncode() throws IotScriptException {
        // 未定义或返回 null → 不写设备
        assertNull(IotScriptUtils.validateEncode(null, IotProtocolTypeEnum.TCP));
        // 走标准路径
        assertTrue(IotScriptUtils.validateEncode("{\"standard\":true}", IotProtocolTypeEnum.TCP).isStandard());
        // 字节
        IotScriptUtils.EncodeOutput output = IotScriptUtils.validateEncode("{\"bytes\":[1,2,255]}", IotProtocolTypeEnum.TCP);
        assertFalse(output.isStandard());
        assertArrayEquals(new byte[]{1, 2, (byte) 0xFF}, output.getBytes());
        assertEquals(4096, IotScriptUtils.validateEncode("{\"bytes\":" + buildArray(4096, 0) + "}",
                IotProtocolTypeEnum.TCP).getBytes().length);
        // 不合法：standard=false、两者都有、没有 bytes、不是数组、超过 255
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateEncode("{\"standard\":false}", IotProtocolTypeEnum.TCP));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateEncode("{\"standard\":true,\"bytes\":[1]}", IotProtocolTypeEnum.TCP));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateEncode("{}", IotProtocolTypeEnum.TCP));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateEncode("{\"bytes\":\"AQI=\"}", IotProtocolTypeEnum.TCP));
        assertThrows(IotScriptException.class, () -> IotScriptUtils.validateEncode("{\"bytes\":[256]}", IotProtocolTypeEnum.TCP));
    }

    @Test
    public void testValidateEncode_rawPayload() throws IotScriptException {
        // 调用 + 断言：其它协议 1~4096 字节，不合法为 OUTPUT_INVALID，带原规则的错误信息
        assertEncodeOutputInvalid("{\"bytes\":[]}", IotProtocolTypeEnum.TCP, "raw 长度必须在 1~4096 字节之间");
        assertEncodeOutputInvalid("{\"bytes\":" + buildArray(4097, 0) + "}", IotProtocolTypeEnum.TCP,
                "raw 长度必须在 1~4096 字节之间");

        // 调用 + 断言：Modbus 必须是合法的请求 PDU
        assertArrayEquals(new byte[]{3, 0, 0, 0, 10}, IotScriptUtils.validateEncode("{\"bytes\":[3,0,0,0,10]}",
                IotProtocolTypeEnum.MODBUS_TCP_CLIENT).getBytes());
        assertEncodeOutputInvalid("{\"bytes\":[3,0,0,0,0]}", IotProtocolTypeEnum.MODBUS_TCP_SERVER,
                "PDU 不合法：读寄存器数量必须在 1~125 之间");
        // 调用 + 断言：走标准路径时没有字节，不按协议的规则校验
        assertTrue(IotScriptUtils.validateEncode("{\"standard\":true}", IotProtocolTypeEnum.MODBUS_TCP_CLIENT).isStandard());
    }

    private static void assertOutputInvalid(String json) {
        IotScriptException ex = assertThrows(IotScriptException.class,
                () -> IotScriptUtils.validateDecode(json, null), json);
        assertEquals(IotScriptResult.Status.OUTPUT_INVALID, ex.getStatus());
    }

    private static void assertEncodeOutputInvalid(String json, IotProtocolTypeEnum protocol, String message) {
        IotScriptException ex = assertThrows(IotScriptException.class,
                () -> IotScriptUtils.validateEncode(json, protocol));
        assertEquals(IotScriptResult.Status.OUTPUT_INVALID, ex.getStatus());
        assertEquals(message, ex.getMessage());
    }

    private static String buildArray(int size, int value) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(i > 0 ? "," : "").append(value);
        }
        return sb.append("]").toString();
    }

}
