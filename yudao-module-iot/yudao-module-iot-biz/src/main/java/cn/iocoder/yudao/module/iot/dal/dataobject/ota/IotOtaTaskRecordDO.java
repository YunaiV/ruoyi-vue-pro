package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT OTA 升级任务记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_ota_task_record", autoResultMap = true)
@KeySequence("iot_ota_task_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOtaTaskRecordDO extends BaseDO {

    public static final String DESCRIPTION_CANCEL_BY_TASK = "管理员手动取消升级任务（批量）";

    public static final String DESCRIPTION_CANCEL_BY_RECORD = "管理员手动取消升级记录（单个）";

    /**
     * 升级记录编号
     */
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
     * 关联 {@link IotOtaTaskDO#getId()}
     */
    private Long taskId;

    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 来源的固件编号
     *
     * 关联 {@link IotDeviceDO#getFirmwareId()}
     */
    private Long fromFirmwareId;

    /**
     * 升级状态
     *
     * 关联 {@link IotOtaTaskRecordStatusEnum}
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
     * 如果想看历史记录，可以查看 {@link IotDeviceMessageDO} 设备日志
     */
    private String description;

}