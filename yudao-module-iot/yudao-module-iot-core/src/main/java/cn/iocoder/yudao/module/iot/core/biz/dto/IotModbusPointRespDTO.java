package cn.iocoder.yudao.module.iot.core.biz.dto;

import cn.iocoder.yudao.module.iot.core.enums.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFunctionCodeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusRawDataTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * IoT Modbus 点位配置 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class IotModbusPointRespDTO {

    /**
     * 点位编号
     */
    private Long id;
    /**
     * 属性标识符（物模型的 identifier）
     */
    private String identifier;
    /**
     * 属性名称（物模型的 name）
     */
    private String name;

    // ========== Modbus 协议配置 ==========

    /**
     * Modbus 功能码
     *
     * 枚举 {@link IotModbusFunctionCodeEnum}
     */
    private Integer functionCode;
    /**
     * 寄存器起始地址
     */
    private Integer registerAddress;
    /**
     * 寄存器数量
     */
    private Integer registerCount;
    /**
     * 字节序
     *
     * 枚举 {@link IotModbusByteOrderEnum}
     */
    private String byteOrder;
    /**
     * 原始数据类型
     *
     * 枚举 {@link IotModbusRawDataTypeEnum}
     */
    private String rawDataType;
    /**
     * 缩放因子
     */
    private BigDecimal scale;
    /**
     * 轮询间隔（毫秒）
     */
    private Integer pollInterval;

}
