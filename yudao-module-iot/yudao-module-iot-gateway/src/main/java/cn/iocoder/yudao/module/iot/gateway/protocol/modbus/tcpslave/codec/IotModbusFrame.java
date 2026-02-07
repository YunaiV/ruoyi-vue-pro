package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec;

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
     * 事务标识符（TCP 模式特有）
     *
     * // TODO @AI：最好是 @某个类型独有；
     */
    private Integer transactionId;

    /**
     * 是否异常响应
     */
    private boolean exception;
    // TODO @AI：是不是要枚举一些异常；另外，是不是覆盖掉 exception；因为只要判断有异常码是不是就可以了；
    /**
     * 异常码（当 exception=true 时有效）
     */
    private Integer exceptionCode;

    /**
     * 自定义功能码时的 JSON 字符串
     */
    private String customData;

}
