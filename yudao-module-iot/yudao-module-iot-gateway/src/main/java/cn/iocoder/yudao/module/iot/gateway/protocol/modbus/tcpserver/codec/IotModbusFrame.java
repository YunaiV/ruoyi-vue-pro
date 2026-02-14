package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec;

import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IoT Modbus 统一帧数据模型（TCP/RTU 公用）
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class IotModbusFrame {

    /**
     * 从站地址
     */
    private int slaveId;
    /**
     * 功能码
     */
    private int functionCode;
    /**
     * PDU 数据（不含 slaveId）
     */
    private byte[] pdu;
    /**
     * 事务标识符
     * <p>
     * 仅 {@link IotModbusFrameFormatEnum#MODBUS_TCP} 格式有值
     */
    private Integer transactionId;

    /**
     * 异常码
     * <p>
     * 当功能码最高位为 1 时（异常响应），此字段存储异常码。
     *
     * @see IotModbusCommonUtils#FC_EXCEPTION_MASK
     */
    private Integer exceptionCode;

    /**
     * 自定义功能码时的 JSON 字符串（用于 auth 认证等等）
     */
    private String customData;

    /**
     * 是否异常响应（基于 exceptionCode 是否有值判断）
     */
    public boolean isException() {
        return exceptionCode != null;
    }

}
