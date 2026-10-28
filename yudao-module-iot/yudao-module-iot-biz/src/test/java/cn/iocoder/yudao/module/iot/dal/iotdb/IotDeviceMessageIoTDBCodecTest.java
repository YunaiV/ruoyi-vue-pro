package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceMessageIoTDBCodec} 的单元测试
 *
 * @author 芋道源码
 */
public class IotDeviceMessageIoTDBCodecTest {

    @Test
    public void testEncode_raw() {
        // 调用 + 断言：有原始帧
        IotDeviceMessageIoTDBRow row = IotDeviceMessageIoTDBCodec.encode(buildMessage().setRaw("AQID"));
        assertEquals("v:AQID", row.getRaw());
        // 调用 + 断言：没有原始帧，也写入非 NULL 值，保证同一 (device_id, time) 整行替换
        IotDeviceMessageIoTDBRow nullRow = IotDeviceMessageIoTDBCodec.encode(buildMessage());
        assertEquals("n:", nullRow.getRaw());
    }

    @Test
    public void testDecode_raw() {
        // 调用 + 断言：v: 为值，n: 为 null
        assertEquals("AQID", IotDeviceMessageIoTDBCodec.decode(buildRow().setRaw("v:AQID")).getRaw());
        assertNull(IotDeviceMessageIoTDBCodec.decode(buildRow().setRaw("n:")).getRaw());
    }

    @Test
    public void testDecode_rawCorrupted() {
        // 调用 + 断言：不是本系统的编码，按损坏处理，不静默
        assertThrows(IllegalArgumentException.class,
                () -> IotDeviceMessageIoTDBCodec.decode(buildRow().setRaw("AQID")));
    }

    @Test
    public void testEncodeDecode_roundTrip() {
        // 准备参数
        IotDeviceMessageDO message = buildMessage().setRaw("AQID");

        // 调用
        IotDeviceMessageDO result = IotDeviceMessageIoTDBCodec.decode(IotDeviceMessageIoTDBCodec.encode(message));
        // 断言
        assertEquals(message.getRaw(), result.getRaw());
        assertEquals(message.getId(), result.getId());
        assertEquals(message.getDeviceId(), result.getDeviceId());
        assertEquals(message.getMethod(), result.getMethod());
        assertEquals(message.getUpstream(), result.getUpstream());
        assertNull(result.getParams());
    }

    private static IotDeviceMessageDO buildMessage() {
        return new IotDeviceMessageDO().setTs(1000L).setDeviceId(2L).setId("msg-1")
                .setMethod("thing.property.post").setUpstream(true).setReply(false);
    }

    private static IotDeviceMessageIoTDBRow buildRow() {
        return new IotDeviceMessageIoTDBRow().setTs(1000L).setDeviceId("2").setId("msg-1")
                .setUpstream(1).setReply(0).setMethod("v:thing.property.post").setIdentifier("n:")
                .setRequestId("n:").setServerId("n:").setParams("n:").setData("n:").setMsg("n:")
                .setReportTime("n:").setTenantId("n:").setCode("n:");
    }

}
