package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 设备 Modbus 连接配置 DO
 *
 * @author 芋道源码
 */
@TableName("iot_device_modbus_config")
@KeySequence("iot_device_modbus_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceModbusConfigDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品编号
     *
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * Modbus 服务器 IP 地址
     */
    private String ip;
    /**
     * Modbus 服务器端口
     */
    private Integer port;
    /**
     * 从站地址
     */
    private Integer slaveId;
    /**
     * 连接超时时间，单位：毫秒
     */
    private Integer timeout;
    /**
     * 重试间隔，单位：毫秒
     */
    private Integer retryInterval;
    /**
     * 模式
     *
     * @see IotModbusModeEnum
     */
    private Integer mode;
    /**
     * 数据帧格式
     *
     * @see IotModbusFrameFormatEnum
     */
    private Integer frameFormat;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
