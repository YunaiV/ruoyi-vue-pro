package cn.iocoder.yudao.module.iot.service.ota.bo.upgrade.record;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotOtaUpgradeRecordCreateReqBO {

    /**
     * 固件编号
     * <p>
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    @NotNull(message = "固件编号不能为空")
    private Long firmwareId;
    /**
     * 任务编号
     * <p>
     * 关联 {@link IotOtaUpgradeTaskDO#getId()}
     */
    @NotNull(message = "任务编号不能为空")
    private Long taskId;
    /**
     * 产品标识
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    private String productKey;
    /**
     * 设备名称
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    private String deviceName;
    /**
     * 设备编号
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    @NotNull(message = "设备编号不能为空")
    private String deviceId;
    /**
     * 来源的固件编号
     * <p>
     * 关联 {@link IotDeviceDO#getFirmwareId()}
     */
    private Long fromFirmwareId;
    /**
     * 升级状态
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum}
     */
    private Integer status;
    /**
     * 升级进度，百分比
     */
    private Integer progress;
    /**
     * 升级进度描述
     * <p>
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
