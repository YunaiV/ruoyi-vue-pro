package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
     * 升级状态
     *
     * TODO
     */
    private Integer status;
    /**
     * 升级进度，百分比
     */
    private Integer progress;

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
     * 升级开始时间
     */
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    private LocalDateTime endTime;



}