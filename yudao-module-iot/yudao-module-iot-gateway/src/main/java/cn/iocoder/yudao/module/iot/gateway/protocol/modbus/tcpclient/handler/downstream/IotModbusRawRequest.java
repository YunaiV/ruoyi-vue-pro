package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_RESPONSE_INVALID;

/**
 * Modbus 原始 PDU 请求（只用于透传，主站发送）
 * <p>
 * j2mod 3.4.0 的 ModbusTCPTransport.writeMessage 按 [MBAP][unitId][getFunctionCode()][getMessage()] 写出，
 * 所以 {@link #getMessage()} 返回 PDU 去掉功能码之后的数据部分（不带 unit、MBAP）。
 * 响应由 ModbusTCPTransport.readResponse 按功能码用 ModbusResponse.createModbusResponse 创建（不经过 {@link #getResponse()}），
 * 白名单内的 8 个功能码（01-06、0F、10）都有对应的标准响应类，{@link #buildResponsePdu} 用它的 getMessage 还原数据部分；
 * 异常响应由 ModbusTCPTransaction.execute 转为 ModbusSlaveException，调用方再用 transaction.getResponse() 拿到真实的 ExceptionResponse 交给这里。
 * 正常、异常响应都先经过 {@link #isResponseOf} 核对身份
 *
 * @author 芋道源码
 */
public class IotModbusRawRequest extends ModbusRequest {

    /**
     * 完整的请求 PDU（功能码 + 数据）
     */
    private final byte[] pdu;
    /**
     * PDU 去掉功能码之后的部分
     */
    private final byte[] data;

    public IotModbusRawRequest(byte[] pdu) {
        this.pdu = pdu.clone();
        this.data = Arrays.copyOfRange(pdu, 1, pdu.length);
        setFunctionCode(pdu[0] & 0xFF);
        setDataLength(data.length);
    }

    /**
     * TCP 主站不经过这里；按 j2mod 的约定返回带请求头的空响应
     */
    @Override
    public ModbusResponse getResponse() {
        ModbusResponse response = ModbusResponse.createModbusResponse(getFunctionCode());
        response.setHeadless(isHeadless());
        response.setTransactionID(getTransactionID());
        response.setProtocolID(getProtocolID());
        response.setUnitID(getUnitID());
        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        throw new UnsupportedOperationException("透传请求只用于主站发送");
    }

    @Override
    public void writeData(DataOutput dout) throws IOException {
        dout.write(data);
    }

    @Override
    public void readData(DataInput din) {
        throw new UnsupportedOperationException("透传请求只用于主站发送");
    }

    @Override
    public byte[] getMessage() {
        return data;
    }

    /**
     * 响应是否属于这次发出的请求：事务号、unit、功能码（异常响应为 fc | 0x80）都一致
     * <p>
     * j2mod 只在正常响应时核对事务号（异常响应在核对之前就抛出 ModbusSlaveException），unit 都不核对，所以这里逐项核对。
     * 发出的身份要在 setRequest 之后、execute 之前保存：execute 结束后 j2mod 会把请求的事务号自增，不能再用请求本身核对
     *
     * @param response      j2mod 响应（transaction.getResponse()，正常或异常响应）
     * @param transactionId 发出的事务号
     * @param unitId        发出的 unit
     * @param functionCode  发出的功能码
     * @return 是否属于这次请求
     */
    public static boolean isResponseOf(ModbusResponse response, int transactionId, int unitId, int functionCode) {
        if (response == null) {
            return false;
        }
        int expectedFunctionCode = response instanceof ExceptionResponse
                ? functionCode | IotModbusCommonUtils.FC_EXCEPTION_MASK : functionCode;
        return response.getTransactionID() == transactionId
                && response.getUnitID() == unitId
                && response.getFunctionCode() == expectedFunctionCode;
    }

    /**
     * 从 j2mod 解析出的响应还原响应 PDU，并核对功能码和响应格式
     *
     * @param response j2mod 响应（正常或异常响应）
     * @return 响应 PDU：异常 → [fc | 0x80, 异常码]；正常 → 功能码 + 数据
     */
    public byte[] buildResponsePdu(ModbusResponse response) {
        // 1.1 异常响应：功能码必须是 fc | 0x80（ExceptionResponse.getMessage 是 1 字节异常码）
        if (response instanceof ExceptionResponse) {
            if (response.getFunctionCode() != (getFunctionCode() | IotModbusCommonUtils.FC_EXCEPTION_MASK)) {
                throw exception(MODBUS_RESPONSE_INVALID);
            }
            return IotModbusCommonUtils.buildExceptionPdu(getFunctionCode(),
                    ((ExceptionResponse) response).getExceptionCode());
        }
        // 1.2 正常响应：功能码必须与请求一致
        if (response == null || response.getFunctionCode() != getFunctionCode()) {
            throw exception(MODBUS_RESPONSE_INVALID);
        }
        // 2. 还原 PDU：功能码 + 标准响应类重新序列化的数据部分
        byte[] message = response.getMessage();
        byte[] responsePdu = new byte[1 + message.length];
        responsePdu[0] = (byte) getFunctionCode();
        System.arraycopy(message, 0, responsePdu, 1, message.length);
        // 3. 核对响应格式（数量、回显）
        if (!IotModbusCommonUtils.isResponseShapeValid(pdu, responsePdu)) {
            throw exception(MODBUS_RESPONSE_INVALID);
        }
        return responsePdu;
    }

}
