package cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotUdpUpstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotUdpUpstreamHandlerTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.e30.token";
    private static final byte[] PAYLOAD = new byte[]{0x01, 0x02, 0x7E, 0x7B};

    @Test
    public void testIsRawPacket() {
        assertTrue(IotUdpUpstreamHandler.isRawPacket(buildUpstreamPacket(TOKEN, PAYLOAD)));
        // JSON、二进制序列化、长度不足、空
        assertFalse(IotUdpUpstreamHandler.isRawPacket(StrUtil.utf8Bytes("{\"method\":\"auth\"}")));
        assertFalse(IotUdpUpstreamHandler.isRawPacket(new byte[]{0x7E, 0x01, 0x02}));
        assertFalse(IotUdpUpstreamHandler.isRawPacket(new byte[]{0x49}));
        assertFalse(IotUdpUpstreamHandler.isRawPacket(new byte[0]));
        assertFalse(IotUdpUpstreamHandler.isRawPacket(null));
    }

    @Test
    public void testDecodeRawPacket_success() {
        // 准备参数
        byte[] data = buildUpstreamPacket(TOKEN, PAYLOAD);

        // 调用
        IotUdpUpstreamHandler.RawPacket packet = IotUdpUpstreamHandler.decodeRawPacket(data);
        // 断言
        assertEquals(TOKEN, packet.getToken());
        assertArrayEquals(PAYLOAD, packet.getPayload());
    }

    @Test
    public void testDecodeRawPacket_maxPayload() {
        // 准备参数：payload 正好 4096 字节
        byte[] payload = new byte[IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES];
        payload[payload.length - 1] = 0x55;

        // 调用
        IotUdpUpstreamHandler.RawPacket packet = IotUdpUpstreamHandler.decodeRawPacket(buildUpstreamPacket(TOKEN, payload));
        // 断言
        assertArrayEquals(payload, packet.getPayload());
    }

    @Test
    public void testDecodeRawPacket_invalid() {
        byte[] valid = buildUpstreamPacket(TOKEN, PAYLOAD);
        // 魔术字不正确
        assertThrows(IllegalArgumentException.class,
                () -> IotUdpUpstreamHandler.decodeRawPacket(StrUtil.utf8Bytes("{\"method\":\"auth\"}")));
        // 报文头不完整
        assertThrows(IllegalArgumentException.class,
                () -> IotUdpUpstreamHandler.decodeRawPacket(new byte[]{0x49, 0x52, 0x01, 0x00}));
        // 版本不支持
        byte[] wrongVersion = valid.clone();
        wrongVersion[2] = 0x02;
        assertThrows(IllegalArgumentException.class, () -> IotUdpUpstreamHandler.decodeRawPacket(wrongVersion));
        // 缺少 token（tokenLen = 0，例如误把下行报文发上来）
        assertThrows(IllegalArgumentException.class,
                () -> IotUdpUpstreamHandler.decodeRawPacket(IotUdpUpstreamHandler.encodeRawDownstream(PAYLOAD)));
        // tokenLen 超过报文长度
        byte[] tokenTooLong = valid.clone();
        tokenTooLong[3] = (byte) 0xFF;
        assertThrows(IllegalArgumentException.class, () -> IotUdpUpstreamHandler.decodeRawPacket(tokenTooLong));
        // 缺少 payload
        assertThrows(IllegalArgumentException.class,
                () -> IotUdpUpstreamHandler.decodeRawPacket(buildUpstreamPacket(TOKEN, new byte[0])));
        // payload 超过 4096 字节
        assertThrows(IllegalArgumentException.class, () -> IotUdpUpstreamHandler.decodeRawPacket(
                buildUpstreamPacket(TOKEN, new byte[IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES + 1])));
    }

    @Test
    public void testEncodeRawDownstream() {
        // 调用
        byte[] data = IotUdpUpstreamHandler.encodeRawDownstream(PAYLOAD);
        // 断言：0x49 0x52 | 0x01 | 0x0000 | payload
        assertArrayEquals(ArrayUtil.addAll(new byte[]{0x49, 0x52, 0x01, 0x00, 0x00}, PAYLOAD), data);
        assertTrue(IotUdpUpstreamHandler.isRawPacket(data));
    }

    @Test
    public void testEncodeDecode_roundTrip() {
        // 准备参数：设备上行的报文
        byte[] upstream = buildUpstreamPacket(TOKEN, PAYLOAD);

        // 调用：解码上行，再把解出的 payload 编码为下行（例如 ack 回显）
        IotUdpUpstreamHandler.RawPacket packet = IotUdpUpstreamHandler.decodeRawPacket(upstream);
        byte[] downstream = IotUdpUpstreamHandler.encodeRawDownstream(packet.getPayload());
        // 断言：payload 原样保留，下行报文头 tokenLen = 0
        assertEquals(TOKEN, packet.getToken());
        assertArrayEquals(ArrayUtil.sub(upstream, upstream.length - PAYLOAD.length, upstream.length),
                ArrayUtil.sub(downstream, 5, downstream.length));
        assertEquals(0, downstream[3]);
        assertEquals(0, downstream[4]);
    }

    /**
     * 按协议构建上行报文：0x49 0x52 | 0x01 | tokenLen(u16 BE) | token(UTF-8) | payload
     */
    private static byte[] buildUpstreamPacket(String token, byte[] payload) {
        byte[] tokenBytes = StrUtil.utf8Bytes(token);
        byte[] header = new byte[]{0x49, 0x52, 0x01,
                (byte) ((tokenBytes.length >> 8) & 0xFF), (byte) (tokenBytes.length & 0xFF)};
        return ArrayUtil.addAll(header, tokenBytes, payload);
    }

}
