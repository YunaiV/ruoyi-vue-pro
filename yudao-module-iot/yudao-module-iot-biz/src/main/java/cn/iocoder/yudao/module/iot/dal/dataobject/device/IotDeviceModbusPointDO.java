package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * IoT 设备 Modbus 点位配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_device_modbus_point")
@KeySequence("iot_device_modbus_point_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceModbusPointDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 物模型属性编号
     *
     * 关联 {@link IotThingModelDO#getId()}
     */
    private Long thingModelId;
    /**
     * 属性标识符
     *
     * 冗余 {@link IotThingModelDO#getIdentifier()}
     */
    private String identifier;
    /**
     * 属性名称
     *
     * 冗余 {@link IotThingModelDO#getName()}
     */
    private String name;

    // ========== Modbus 协议配置 ==========

    /**
     * Modbus 功能码
     *
     * 取值范围：FC01-04（读线圈、读离散输入、读保持寄存器、读输入寄存器）
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
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
