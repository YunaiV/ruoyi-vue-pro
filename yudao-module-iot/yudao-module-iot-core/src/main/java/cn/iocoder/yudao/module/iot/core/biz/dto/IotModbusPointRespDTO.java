package cn.iocoder.yudao.module.iot.core.biz.dto;

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

    // TODO @AI：所有的枚举，通过 @，不要写上去；
    /**
     * Modbus 功能码
     *
     * 1-ReadCoils 2-ReadDiscreteInputs 3-ReadHoldingRegisters 4-ReadInputRegisters
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
     * AB/BA/ABCD/CDAB/DCBA/BADC
     */
    private String byteOrder;
    /**
     * 原始数据类型
     *
     * INT16/UINT16/INT32/UINT32/FLOAT/DOUBLE/BOOLEAN/STRING
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

    // ========== 物模型相关字段 ==========

    // TODO @AI：分析一下，是否有必要返回
    /**
     * 数据类型（来自物模型）
     */
    private String dataType;

}
