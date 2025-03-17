package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT OTA 升级记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_ota_upgrade_record", autoResultMap = true)
@KeySequence("iot_ota_upgrade_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOtaUpgradeRecordDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 固件编号
     *
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    private Long firmwareId;
    /**
     * 任务编号
     *
     * 关联 {@link IotOtaUpgradeTaskDO#getId()}
     */
    private Long taskId;

    /**
     * 产品标识
     *
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    private String productKey;
    /**
     * 设备名称
     *
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    private String deviceName;
    /**
     * 设备编号
     *
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    private String deviceId;
    /**
     * 来源的固件编号
     *
     * 关联 {@link IotDeviceDO#getFirmwareId()}
     */
    private Long fromFirmwareId;

    /**
     * 升级状态
     *
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum}
     */
    private Integer status;
    /**
     * 升级进度，百分比
     */
    private Integer progress;
    /**
     * 升级进度描述
     *
     * 注意，只记录设备最后一次的升级进度描述
     * 如果想看历史记录，可以查看 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO} 设备日志
     */
    private String description;
    /**
     * 升级开始时间
     */
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    private LocalDateTime endTime;

}