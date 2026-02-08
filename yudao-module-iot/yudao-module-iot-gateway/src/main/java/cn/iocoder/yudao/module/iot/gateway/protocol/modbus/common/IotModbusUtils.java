package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus 工具类
 * <p>
 * 提供：
 * 1. Modbus 协议常量（功能码、掩码等）
 * 2. CRC-16/MODBUS 计算和校验
 * 3. 功能码分类判断
 * 4. 从解码后的 ${IotModbusFrame} 中提取寄存器值（用于后续的点位翻译）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusUtils {

    /** FC01: 读线圈 */
    public static final int FC_READ_COILS = 1;
    /** FC02: 读离散输入 */
    public static final int FC_READ_DISCRETE_INPUTS = 2;
    /** FC03: 读保持寄存器 */
    public static final int FC_READ_HOLDING_REGISTERS = 3;
    /** FC04: 读输入寄存器 */
    public static final int FC_READ_INPUT_REGISTERS = 4;

    /** FC05: 写单个线圈 */
    public static final int FC_WRITE_SINGLE_COIL = 5;
    /** FC06: 写单个寄存器 */
    public static final int FC_WRITE_SINGLE_REGISTER = 6;
    /** FC15: 写多个线圈 */
    public static final int FC_WRITE_MULTIPLE_COILS = 15;
    /** FC16: 写多个寄存器 */
    public static final int FC_WRITE_MULTIPLE_REGISTERS = 16;

    /**
     * 异常响应掩码：响应帧的功能码最高位为 1 时，表示异常响应
     * 例如：请求 FC=0x03，异常响应 FC=0x83（0x03 | 0x80）
     */
    public static final int FC_EXCEPTION_MASK = 0x80;

    /**
     * 功能码掩码：用于从异常响应中提取原始功能码
     * 例如：异常 FC=0x83，原始 FC = 0x83 & 0x7F = 0x03
     */
    public static final int FC_MASK = 0x7F;

    // ==================== 功能码分类判断 ====================

    /**
     * 判断是否为读响应（FC01-04）
     */
    public static boolean isReadResponse(int functionCode) {
        return functionCode >= FC_READ_COILS && functionCode <= FC_READ_INPUT_REGISTERS;
    }

    /**
     * 判断是否为写响应（FC05/06/15/16）
     */
    public static boolean isWriteResponse(int functionCode) {
        return functionCode == FC_WRITE_SINGLE_COIL || functionCode == FC_WRITE_SINGLE_REGISTER
                || functionCode == FC_WRITE_MULTIPLE_COILS || functionCode == FC_WRITE_MULTIPLE_REGISTERS;
    }

    /**
     * 判断是否为异常响应
     */
    public static boolean isExceptionResponse(int functionCode) {
        return (functionCode & FC_EXCEPTION_MASK) != 0;
    }

    /**
     * 从异常响应中提取原始功能码
     */
    public static int extractOriginalFunctionCode(int exceptionFunctionCode) {
        return exceptionFunctionCode & FC_MASK;
    }

    /**
     * 判断读功能码是否支持写操作
     * <p>
     * FC01（读线圈）和 FC03（读保持寄存器）支持写操作；
     * FC02（读离散输入）和 FC04（读输入寄存器）为只读。
     *
     * @param readFunctionCode 读功能码（FC01-04）
     * @return 是否支持写操作
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWritable(int readFunctionCode) {
        return readFunctionCode == FC_READ_COILS || readFunctionCode == FC_READ_HOLDING_REGISTERS;
    }

    /**
     * 获取单写功能码
     * <p>
     * FC01（读线圈）→ FC05（写单个线圈）；
     * FC03（读保持寄存器）→ FC06（写单个寄存器）；
     * 其他返回 null（不支持写）。
     *
     * @param readFunctionCode 读功能码
     * @return 单写功能码，不支持写时返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static Integer getWriteSingleFunctionCode(int readFunctionCode) {
        switch (readFunctionCode) {
            case FC_READ_COILS:
                return FC_WRITE_SINGLE_COIL;
            case FC_READ_HOLDING_REGISTERS:
                return FC_WRITE_SINGLE_REGISTER;
            default:
                return null;
        }
    }

    /**
     * 获取多写功能码
     * <p>
     * FC01（读线圈）→ FC15（写多个线圈）；
     * FC03（读保持寄存器）→ FC16（写多个寄存器）；
     * 其他返回 null（不支持写）。
     *
     * @param readFunctionCode 读功能码
     * @return 多写功能码，不支持写时返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static Integer getWriteMultipleFunctionCode(int readFunctionCode) {
        switch (readFunctionCode) {
            case FC_READ_COILS:
                return FC_WRITE_MULTIPLE_COILS;
            case FC_READ_HOLDING_REGISTERS:
                return FC_WRITE_MULTIPLE_REGISTERS;
            default:
                return null;
        }
    }

    // ==================== 点位查找 ====================

    /**
     * 查找点位配置
     *
     * @param config     设备 Modbus 配置
     * @param identifier 点位标识符
     * @return 匹配的点位配置，未找到返回 null
     */
    public static IotModbusPointRespDTO findPoint(IotModbusDeviceConfigRespDTO config, String identifier) {
        return CollUtil.findOne(config.getPoints(), p -> identifier.equals(p.getIdentifier()));
    }

    // ==================== CRC16 工具 ====================

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

    /**
     * 校验 CRC16
     *
     * @param data 包含 CRC 的完整数据
     * @return 校验是否通过
     */
    public static boolean verifyCrc16(byte[] data) {
        if (data.length < 3) {
            return false;
        }
        int computed = calculateCrc16(data, data.length - 2);
        int received = (data[data.length - 2] & 0xFF) | ((data[data.length - 1] & 0xFF) << 8);
        return computed == received;
    }

    // ==================== 响应值提取 ====================

    /**
     * 从帧中提取寄存器值（FC01-04 读响应）
     *
     * @param frame 解码后的 Modbus 帧
     * @return 寄存器值数组（int[]），失败返回 null
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static int[] extractValues(IotModbusFrame frame) {
        if (frame == null || frame.isException()) {
            return null;
        }
        byte[] pdu = frame.getPdu();
        if (pdu == null || pdu.length < 1) {
            return null;
        }

        int functionCode = frame.getFunctionCode();
        switch (functionCode) {
            case FC_READ_COILS:
            case FC_READ_DISCRETE_INPUTS:
                return extractCoilValues(pdu);
            case FC_READ_HOLDING_REGISTERS:
            case FC_READ_INPUT_REGISTERS:
                return extractRegisterValues(pdu);
            default:
                log.warn("[extractValues][不支持的功能码: {}]", functionCode);
                return null;
        }
    }

    /**
     * 提取线圈/离散输入值
     * PDU 格式（FC01/02 响应）：[ByteCount(1)] [CoilStatus(N)]
     */
    private static int[] extractCoilValues(byte[] pdu) {
        if (pdu.length < 2) {
            return null;
        }
        int byteCount = pdu[0] & 0xFF;
        int bitCount = byteCount * 8;
        int[] values = new int[bitCount];
        for (int i = 0; i < bitCount && (1 + i / 8) < pdu.length; i++) {
            values[i] = ((pdu[1 + i / 8] >> (i % 8)) & 0x01);
        }
        return values;
    }

    /**
     * 提取寄存器值
     * PDU 格式（FC03/04 响应）：[ByteCount(1)] [RegisterData(N*2)]
     */
    private static int[] extractRegisterValues(byte[] pdu) {
        if (pdu.length < 2) {
            return null;
        }
        int byteCount = pdu[0] & 0xFF;
        int registerCount = byteCount / 2;
        int[] values = new int[registerCount];
        for (int i = 0; i < registerCount && (1 + i * 2 + 1) < pdu.length; i++) {
            values[i] = ((pdu[1 + i * 2] & 0xFF) << 8) | (pdu[1 + i * 2 + 1] & 0xFF);
        }
        return values;
    }

}
