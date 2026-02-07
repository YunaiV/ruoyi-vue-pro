package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec;

import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus 响应值提取器
 * <p>
 * 从解码后的 IotModbusFrame 中提取寄存器值，用于后续的点位翻译。
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusResponseParser {

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

        //  TODO @AI：jmodbus 看看有没可以复用的枚举类
        int functionCode = frame.getFunctionCode();
        switch (functionCode) {
            case 1: // Read Coils
            case 2: // Read Discrete Inputs
                return extractCoilValues(pdu);
            case 3: // Read Holding Registers
            case 4: // Read Input Registers
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
