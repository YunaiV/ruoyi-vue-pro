package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec;

import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * IoT Modbus 帧编码器：负责将 Modbus 请求/响应编码为字节数组，支持 MODBUS_TCP（MBAP）和 MODBUS_RTU（CRC16）两种帧格式。
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusFrameEncoder {

    private final int customFunctionCode;

    // ==================== 编码 ====================

    /**
     * 编码读请求
     *
     * @param slaveId       从站地址
     * @param functionCode  功能码
     * @param startAddress  起始寄存器地址
     * @param quantity      寄存器数量
     * @param format        帧格式
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式传 null）
     * @return 编码后的字节数组
     */
    public byte[] encodeReadRequest(int slaveId, int functionCode, int startAddress, int quantity,
                                    IotModbusFrameFormatEnum format, Integer transactionId) {
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
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式传 null）
     * @return 编码后的字节数组
     */
    public byte[] encodeWriteSingleRequest(int slaveId, int functionCode, int address, int value,
                                           IotModbusFrameFormatEnum format, Integer transactionId) {
        // FC05 单写线圈：Modbus 标准要求 value 为 0xFF00（ON）或 0x0000（OFF）
        if (functionCode == IotModbusCommonUtils.FC_WRITE_SINGLE_COIL) {
            value = (value != 0) ? 0xFF00 : 0x0000;
        }
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
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式传 null）
     * @return 编码后的字节数组
     */
    public byte[] encodeWriteMultipleRegistersRequest(int slaveId, int address, int[] values,
                                                      IotModbusFrameFormatEnum format, Integer transactionId) {
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
     * 编码写多个线圈请求（FC15）
     * <p>
     * 按 Modbus FC15 标准，线圈值按 bit 打包（每个 byte 包含 8 个线圈状态）。
     *
     * @param slaveId       从站地址
     * @param address       起始地址
     * @param values        线圈值数组（int[]，非0 表示 ON，0 表示 OFF）
     * @param format        帧格式
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式传 null）
     * @return 编码后的字节数组
     */
    public byte[] encodeWriteMultipleCoilsRequest(int slaveId, int address, int[] values,
                                                   IotModbusFrameFormatEnum format, Integer transactionId) {
        // PDU: [FC(1)] [Address(2)] [Quantity(2)] [ByteCount(1)] [CoilValues(N)]
        int quantity = values.length;
        int byteCount = (quantity + 7) / 8; // 向上取整
        byte[] pdu = new byte[6 + byteCount];
        pdu[0] = (byte) IotModbusCommonUtils.FC_WRITE_MULTIPLE_COILS; // FC15
        pdu[1] = (byte) ((address >> 8) & 0xFF);
        pdu[2] = (byte) (address & 0xFF);
        pdu[3] = (byte) ((quantity >> 8) & 0xFF);
        pdu[4] = (byte) (quantity & 0xFF);
        pdu[5] = (byte) byteCount;
        // 按 bit 打包：每个 byte 的 bit0 对应最低地址的线圈
        for (int i = 0; i < quantity; i++) {
            if (values[i] != 0) {
                pdu[6 + i / 8] |= (byte) (1 << (i % 8));
            }
        }
        return wrapFrame(slaveId, pdu, format, transactionId);
    }

    /**
     * 编码自定义功能码帧（认证响应等）
     *
     * @param slaveId       从站地址
     * @param jsonData      JSON 数据
     * @param format        帧格式
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式传 null）
     * @return 编码后的字节数组
     */
    public byte[] encodeCustomFrame(int slaveId, String jsonData,
                                    IotModbusFrameFormatEnum format, Integer transactionId) {
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
     * @param transactionId 事务 ID（TCP 模式下使用，RTU 模式可为 null）
     * @return 完整帧字节数组
     */
    private byte[] wrapFrame(int slaveId, byte[] pdu, IotModbusFrameFormatEnum format, Integer transactionId) {
        if (format == IotModbusFrameFormatEnum.MODBUS_TCP) {
            return wrapTcpFrame(slaveId, pdu, transactionId != null ? transactionId : 0);
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
        int crc = IotModbusCommonUtils.calculateCrc16(frame, frame.length - 2);
        frame[frame.length - 2] = (byte) (crc & 0xFF);        // CRC Low
        frame[frame.length - 1] = (byte) ((crc >> 8) & 0xFF); // CRC High
        return frame;
    }

}
