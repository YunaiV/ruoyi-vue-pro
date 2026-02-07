package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec;

import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * IoT Modbus 帧编解码器
 * <p>
 * 纯 Modbus 协议编解码，不处理 TCP 粘包（由 RecordParser 处理）。
 * 支持 MODBUS_TCP（MBAP）和 MODBUS_RTU（CRC16）两种帧格式，以及自定义功能码扩展。
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusFrameCodec {

    private final int customFunctionCode;

    public IotModbusFrameCodec(int customFunctionCode) {
        this.customFunctionCode = customFunctionCode;
    }

    // ==================== 解码 ====================

    /**
     * 解码响应帧（拆包后的完整帧 byte[]）
     *
     * @param data   完整帧字节数组
     * @param format 帧格式
     * @return 解码后的 IotModbusFrame
     */
    public IotModbusFrame decodeResponse(byte[] data, IotModbusFrameFormatEnum format) {
        if (format == IotModbusFrameFormatEnum.MODBUS_TCP) {
            return decodeTcpResponse(data);
        } else {
            return decodeRtuResponse(data);
        }
    }

    /**
     * 解码 MODBUS_TCP 响应
     * 格式：[TransactionId(2)] [ProtocolId(2)] [Length(2)] [UnitId(1)] [FC(1)] [Data...]
     */
    private IotModbusFrame decodeTcpResponse(byte[] data) {
        if (data.length < 8) {
            log.warn("[decodeTcpResponse][数据长度不足: {}]", data.length);
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int transactionId = buf.getShort() & 0xFFFF;
        buf.getShort(); // protocolId（跳过）// TODO @AI：跳过原因，最好写下；
        buf.getShort(); // length（跳过）// TODO @AI：跳过原因，最好写下；
        int slaveId = buf.get() & 0xFF;
        int functionCode = buf.get() & 0xFF;
        // 提取 PDU 数据（从 functionCode 之后到末尾）
        byte[] pdu = new byte[data.length - 8];
        System.arraycopy(data, 8, pdu, 0, pdu.length);

        // 构建 IotModbusFrame
        return buildFrame(slaveId, functionCode, pdu, transactionId);
    }

    /**
     * 解码 MODBUS_RTU 响应
     * 格式：[SlaveId(1)] [FC(1)] [Data...] [CRC(2)]
     */
    private IotModbusFrame decodeRtuResponse(byte[] data) {
        if (data.length < 4) {
            log.warn("[decodeRtuResponse][数据长度不足: {}]", data.length);
            return null;
        }
        // 校验 CRC
        if (!verifyCrc16(data)) {
            log.warn("[decodeRtuResponse][CRC 校验失败]");
            return null;
        }
        int slaveId = data[0] & 0xFF;
        int functionCode = data[1] & 0xFF;
        // PDU 数据（不含 slaveId、functionCode、CRC）
        byte[] pdu = new byte[data.length - 4];
        System.arraycopy(data, 2, pdu, 0, pdu.length);

        // 构建 IotModbusFrame
        return buildFrame(slaveId, functionCode, pdu, null);
    }

    /**
     * 构建 IotModbusFrame
     */
    private IotModbusFrame buildFrame(int slaveId, int functionCode, byte[] pdu, Integer transactionId) {
        IotModbusFrame frame = new IotModbusFrame()
                .setSlaveId(slaveId)
                .setFunctionCode(functionCode)
                .setPdu(pdu)
                .setTransactionId(transactionId);

        // 异常响应
        // TODO @AI：0x80 看看是不是要枚举；
        if ((functionCode & 0x80) != 0) {
            frame.setException(true);
            // TODO @AI：0x7f 看看是不是要枚举；
            frame.setFunctionCode(functionCode & 0x7F);
            if (pdu.length >= 1) {
                frame.setExceptionCode(pdu[0] & 0xFF);
            }
            return frame;
        }

        // 自定义功能码
        if (functionCode == customFunctionCode) {
            // data 区格式：[byteCount(1)] [JSON data(N)]
            if (pdu.length >= 1) {
                int byteCount = pdu[0] & 0xFF;
                if (pdu.length >= 1 + byteCount) {
                    frame.setCustomData(new String(pdu, 1, byteCount, StandardCharsets.UTF_8));
                }
            }
        }
        return frame;
    }

    // ==================== 编码 ====================

    /**
     * 编码读请求
     *
     * @param slaveId       从站地址
     * @param functionCode  功能码
     * @param startAddress  起始寄存器地址
     * @param quantity      寄存器数量
     * @param format        帧格式
     * @param transactionId 事务 ID（TCP 模式下使用）
     * @return 编码后的字节数组
     */
    public byte[] encodeReadRequest(int slaveId, int functionCode, int startAddress, int quantity,
                                    IotModbusFrameFormatEnum format, int transactionId) {
        // PDU: [FC(1)] [StartAddress(2)] [Quantity(2)]
        byte[] pdu = new byte[5];
        pdu[0] = (byte) functionCode;
        pdu[1] = (byte) ((startAddress >> 8) & 0xFF);
        pdu[2] = (byte) (startAddress & 0xFF);
        pdu[3] = (byte) ((quantity >> 8) & 0xFF);
        pdu[4] = (byte) (quantity & 0xFF);
        return wrapFrame(slaveId, pdu, format, transactionId);
    }

    /**
     * 编码写请求（单个寄存器 FC06 / 单个线圈 FC05）
     *
     * @param slaveId       从站地址
     * @param functionCode  功能码
     * @param address       寄存器地址
     * @param value         值
     * @param format        帧格式
     * @param transactionId 事务 ID
     * @return 编码后的字节数组
     */
    public byte[] encodeWriteSingleRequest(int slaveId, int functionCode, int address, int value,
                                           IotModbusFrameFormatEnum format, int transactionId) {
        // PDU: [FC(1)] [Address(2)] [Value(2)]
        byte[] pdu = new byte[5];
        pdu[0] = (byte) functionCode;
        pdu[1] = (byte) ((address >> 8) & 0xFF);
        pdu[2] = (byte) (address & 0xFF);
        pdu[3] = (byte) ((value >> 8) & 0xFF);
        pdu[4] = (byte) (value & 0xFF);
        return wrapFrame(slaveId, pdu, format, transactionId);
    }

    /**
     * 编码写多个寄存器请求（FC16）
     *
     * @param slaveId       从站地址
     * @param address       起始地址
     * @param values        值数组
     * @param format        帧格式
     * @param transactionId 事务 ID
     * @return 编码后的字节数组
     */
    public byte[] encodeWriteMultipleRegistersRequest(int slaveId, int address, int[] values,
                                                      IotModbusFrameFormatEnum format, int transactionId) {
        // PDU: [FC(1)] [Address(2)] [Quantity(2)] [ByteCount(1)] [Values(N*2)]
        int quantity = values.length;
        int byteCount = quantity * 2;
        byte[] pdu = new byte[6 + byteCount];
        pdu[0] = (byte) 16; // FC16
        pdu[1] = (byte) ((address >> 8) & 0xFF);
        pdu[2] = (byte) (address & 0xFF);
        pdu[3] = (byte) ((quantity >> 8) & 0xFF);
        pdu[4] = (byte) (quantity & 0xFF);
        pdu[5] = (byte) byteCount;
        for (int i = 0; i < quantity; i++) {
            pdu[6 + i * 2] = (byte) ((values[i] >> 8) & 0xFF);
            pdu[6 + i * 2 + 1] = (byte) (values[i] & 0xFF);
        }
        return wrapFrame(slaveId, pdu, format, transactionId);
    }

    /**
     * 编码自定义功能码帧（认证响应等）
     *
     * @param slaveId       从站地址
     * @param jsonData      JSON 数据
     * @param format        帧格式
     * @param transactionId 事务 ID
     * @return 编码后的字节数组
     */
    public byte[] encodeCustomFrame(int slaveId, String jsonData,
                                    IotModbusFrameFormatEnum format, int transactionId) {
        byte[] jsonBytes = jsonData.getBytes(StandardCharsets.UTF_8);
        // PDU: [FC(1)] [ByteCount(1)] [JSON data(N)]
        byte[] pdu = new byte[2 + jsonBytes.length];
        pdu[0] = (byte) customFunctionCode;
        pdu[1] = (byte) jsonBytes.length;
        System.arraycopy(jsonBytes, 0, pdu, 2, jsonBytes.length);
        return wrapFrame(slaveId, pdu, format, transactionId);
    }

    // ==================== 帧封装 ====================

    /**
     * 将 PDU 封装为完整帧
     *
     * @param slaveId       从站地址
     * @param pdu           PDU 数据（含 functionCode）
     * @param format        帧格式
     * @param transactionId 事务 ID（TCP 模式下使用）
     * @return 完整帧字节数组
     */
    private byte[] wrapFrame(int slaveId, byte[] pdu, IotModbusFrameFormatEnum format, int transactionId) {
        if (format == IotModbusFrameFormatEnum.MODBUS_TCP) {
            return wrapTcpFrame(slaveId, pdu, transactionId);
        } else {
            return wrapRtuFrame(slaveId, pdu);
        }
    }

    /**
     * 封装 MODBUS_TCP 帧
     * [TransactionId(2)] [ProtocolId(2,=0x0000)] [Length(2)] [UnitId(1)] [PDU...]
     */
    private byte[] wrapTcpFrame(int slaveId, byte[] pdu, int transactionId) {
        int length = 1 + pdu.length; // UnitId + PDU
        byte[] frame = new byte[6 + length]; // MBAP(6) + UnitId(1) + PDU
        // MBAP Header
        frame[0] = (byte) ((transactionId >> 8) & 0xFF);
        frame[1] = (byte) (transactionId & 0xFF);
        frame[2] = 0; // Protocol ID high
        frame[3] = 0; // Protocol ID low
        frame[4] = (byte) ((length >> 8) & 0xFF);
        frame[5] = (byte) (length & 0xFF);
        // Unit ID
        frame[6] = (byte) slaveId;
        // PDU
        System.arraycopy(pdu, 0, frame, 7, pdu.length);
        return frame;
    }

    /**
     * 封装 MODBUS_RTU 帧
     * [SlaveId(1)] [PDU...] [CRC(2)]
     */
    private byte[] wrapRtuFrame(int slaveId, byte[] pdu) {
        byte[] frame = new byte[1 + pdu.length + 2]; // SlaveId + PDU + CRC
        frame[0] = (byte) slaveId;
        System.arraycopy(pdu, 0, frame, 1, pdu.length);
        // 计算并追加 CRC16
        int crc = calculateCrc16(frame, frame.length - 2);
        frame[frame.length - 2] = (byte) (crc & 0xFF);        // CRC Low
        frame[frame.length - 1] = (byte) ((crc >> 8) & 0xFF); // CRC High
        return frame;
    }

    // ==================== CRC16 工具 ====================

    // TODO @AI：hutool 等，有没工具类可以用
    /**
     * 计算 CRC-16/MODBUS
     *
     * @param data   数据
     * @param length 计算长度
     * @return CRC16 值
     */
    public static int calculateCrc16(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc;
    }

    // TODO @AI：hutool 等，有没工具类可以用
    /**
     * 校验 CRC16
     */
    private boolean verifyCrc16(byte[] data) {
        if (data.length < 3) {
            return false;
        }
        int computed = calculateCrc16(data, data.length - 2);
        int received = (data[data.length - 2] & 0xFF) | ((data[data.length - 1] & 0xFF) << 8);
        return computed == received;
    }

}
