package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_RESPONSE_INVALID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotModbusRawRequest} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusRawRequestTest {

    @Test
    public void testWriteTo_fc03() throws IOException {
        // 准备参数
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("0300100002"));
        request.setUnitID(1);
        request.setTransactionID(7);

        // 调用
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        request.writeTo(new DataOutputStream(out));

        // 断言：[MBAP: 事务号 7、协议 0、长度 6][unit 1][03 00 10 00 02]
        assertArrayEquals(HexUtil.decodeHex("000700000006010300100002"), out.toByteArray());
        // ModbusTCPTransport.writeMessage 写出的是 getMessage()：PDU 去掉功能码之后的部分
        assertArrayEquals(HexUtil.decodeHex("00100002"), request.getMessage());
        assertEquals(3, request.getFunctionCode());
    }

    @Test
    public void testBuildResponsePdu_fc03() throws IOException {
        // 准备参数
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("0300100002"));
        ModbusResponse response = readResponse("000700000007010304" + "00FA0258");

        // 调用 + 断言
        assertArrayEquals(HexUtil.decodeHex("030400FA0258"), request.buildResponsePdu(response));
    }

    @Test
    public void testBuildResponsePdu_fc06() throws IOException {
        // 准备参数
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("06001000DC"));
        ModbusResponse response = readResponse("0007000000060106001000DC");

        // 调用 + 断言：原样回显
        assertArrayEquals(HexUtil.decodeHex("06001000DC"), request.buildResponsePdu(response));
    }

    @Test
    public void testBuildResponsePdu_fc10() throws IOException {
        // 准备参数
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("100010000204000A0102"));
        ModbusResponse response = readResponse("000700000006011000100002");

        // 调用 + 断言：回显地址和数量
        assertArrayEquals(HexUtil.decodeHex("1000100002"), request.buildResponsePdu(response));
    }

    @Test
    public void testBuildResponsePdu_shapeInvalid() throws IOException {
        // 准备参数：读 2 个寄存器，设备只回了 1 个
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("0300100002"));
        ModbusResponse response = readResponse("00070000000501030200FA");

        // 调用 + 断言
        ServiceException ex = assertThrows(ServiceException.class, () -> request.buildResponsePdu(response));
        assertEquals(MODBUS_RESPONSE_INVALID.getCode(), ex.getCode());
    }

    @Test
    public void testBuildResponsePdu_exception() throws IOException {
        // 准备参数：transaction.getResponse() 拿到的真实异常响应 83 02
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("0300100002"));
        ModbusResponse response = readResponse("00070000000301" + "8302");

        // 调用 + 断言：还原为 0x83 + 异常码
        assertArrayEquals(HexUtil.decodeHex("8302"), request.buildResponsePdu(response));
    }

    @Test
    public void testBuildResponsePdu_exceptionFunctionCodeMismatch() throws IOException {
        // 准备参数：FC03 的请求收到 84 02
        IotModbusRawRequest request = new IotModbusRawRequest(HexUtil.decodeHex("0300100002"));
        ModbusResponse response = readResponse("00070000000301" + "8402");

        // 调用 + 断言
        ServiceException ex = assertThrows(ServiceException.class, () -> request.buildResponsePdu(response));
        assertEquals(MODBUS_RESPONSE_INVALID.getCode(), ex.getCode());
    }

    @Test
    public void testIsResponseOf_normal() throws IOException {
        // 准备参数：发出的身份 = 事务号 7、unit 1、FC03
        ModbusResponse response = readResponse("000700000007010304" + "00FA0258");

        // 调用 + 断言：逐项核对事务号、unit、功能码
        assertTrue(IotModbusRawRequest.isResponseOf(response, 7, 1, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 8, 1, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 7, 2, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 7, 1, 4));
        assertFalse(IotModbusRawRequest.isResponseOf(null, 7, 1, 3));
    }

    @Test
    public void testIsResponseOf_exception() throws IOException {
        // 准备参数：j2mod 不核对异常响应的事务号，这里同样逐项核对
        ModbusResponse response = readResponse("00070000000301" + "8302");

        // 调用 + 断言：异常响应的功能码是 fc | 0x80
        assertTrue(IotModbusRawRequest.isResponseOf(response, 7, 1, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 6, 1, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 7, 2, 3));
        assertFalse(IotModbusRawRequest.isResponseOf(response, 7, 1, 4));
    }

    /**
     * 与 ModbusTCPTransport.readResponse 相同：按功能码创建标准响应，再从完整 MBAP 帧读入
     */
    private static ModbusResponse readResponse(String hexFrame) throws IOException {
        byte[] frame = HexUtil.decodeHex(hexFrame);
        ModbusResponse response = ModbusResponse.createModbusResponse(frame[7] & 0xFF);
        response.readFrom(new DataInputStream(new ByteArrayInputStream(frame)));
        return response;
    }

}
