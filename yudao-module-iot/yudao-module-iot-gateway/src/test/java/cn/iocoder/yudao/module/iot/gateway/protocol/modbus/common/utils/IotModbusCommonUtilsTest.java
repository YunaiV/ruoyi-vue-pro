package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_EXCEPTION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotModbusCommonUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusCommonUtilsTest {

    @Test
    public void testBuildResponsePdu_exception() {
        // 准备参数：解码器已把 0x83 还原为原功能码 3，异常码单独存放
        IotModbusFrame frame = new IotModbusFrame().setSlaveId(1).setFunctionCode(3)
                .setPdu(new byte[]{0x02}).setExceptionCode(2);

        // 调用 + 断言：还原 0x80 异常位
        assertArrayEquals(HexUtil.decodeHex("8302"), IotModbusCommonUtils.buildResponsePdu(frame));
    }

    @Test
    public void testBuildResponsePdu_normal() {
        // 准备参数：解码器的 pdu 是功能码之后的数据（含 byteCount）
        IotModbusFrame frame = new IotModbusFrame().setSlaveId(1).setFunctionCode(3)
                .setPdu(HexUtil.decodeHex("0400FA0258"));

        // 调用 + 断言
        assertArrayEquals(HexUtil.decodeHex("030400FA0258"), IotModbusCommonUtils.buildResponsePdu(frame));
    }

    @Test
    public void testIsResponseShapeValid() {
        // FC03 读 2 个寄存器：byteCount = 4
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("0300100002"),
                HexUtil.decodeHex("030400FA0258")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("0300100002"),
                HexUtil.decodeHex("030200FA")));
        // FC01 读 10 个线圈：byteCount = 2
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("010000000A"),
                HexUtil.decodeHex("01020103")));
        // FC06：原样回显
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("06001000DC"),
                HexUtil.decodeHex("06001000DC")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("06001000DC"),
                HexUtil.decodeHex("06001000DD")));
        // FC16：回显地址和数量
        assertTrue(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("100010000204000A0102"),
                HexUtil.decodeHex("1000100002")));
        assertFalse(IotModbusCommonUtils.isResponseShapeValid(HexUtil.decodeHex("100010000204000A0102"),
                HexUtil.decodeHex("1000100001")));
    }

    @Test
    public void testGetRawResponseError() {
        // 异常响应 83 02：MODBUS_EXCEPTION，文案带异常码
        ErrorCode error = IotModbusCommonUtils.getRawResponseError(HexUtil.decodeHex("8302"));
        assertNotNull(error);
        assertEquals(MODBUS_EXCEPTION.getCode(), error.getCode());
        assertEquals(StrUtil.format(MODBUS_EXCEPTION.getMsg(), 2), error.getMsg());
        // 正常响应：没有设备错误
        assertNull(IotModbusCommonUtils.getRawResponseError(HexUtil.decodeHex("030400FA0258")));
    }

}
