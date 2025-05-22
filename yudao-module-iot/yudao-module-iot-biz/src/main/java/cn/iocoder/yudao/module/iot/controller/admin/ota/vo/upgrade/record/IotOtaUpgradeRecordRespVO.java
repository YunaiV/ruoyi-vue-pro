package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - IoT OTA 升级记录 Response VO")
public class IotOtaUpgradeRecordRespVO {

    /**
     * 升级记录编号
     */
    @Schema(description = "升级记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;
    /**
     * 固件编号
     * <p>
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @Trans(type = TransType.SIMPLE, target = IotOtaFirmwareDO.class, fields = {"version"}, refs = {"firmwareVersion"})
    private Long firmwareId;
    /**
     * 固件版本
     */
    @Schema(description = "固件版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    private String firmwareVersion;
    /**
     * 任务编号
     * <p>
     * 关联 {@link IotOtaUpgradeTaskDO#getId()}
     */
    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long taskId;
    /**
     * 产品标识
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "iot")
    private String productKey;
    /**
     * 设备名称
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "iot")
    private String deviceName;
    /**
     * 设备编号
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO#getId()}
     */
    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String deviceId;
    /**
     * 来源的固件编号
     * <p>
     * 关联 {@link IotDeviceDO#getFirmwareId()}
     */
    @Schema(description = "来源的固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @Trans(type = TransType.SIMPLE, target = IotOtaFirmwareDO.class, fields = {"version"}, refs = {"fromFirmwareVersion"})
    private Long fromFirmwareId;
    /**
     * 来源的固件版本
     */
    @Schema(description = "来源的固件版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    private String fromFirmwareVersion;
    /**
     * 升级状态
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum}
     */
    @Schema(description = "升级状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"0", "10", "20", "30", "40", "50"})
    private Integer status;
    /**
     * 升级进度，百分比
     */
    @Schema(description = "升级进度，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer progress;
    /**
     * 升级进度描述
     * <p>
     * 注意，只记录设备最后一次的升级进度描述
     * 如果想看历史记录，可以查看 {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO} 设备日志
     */
    @Schema(description = "升级进度描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private String description;
    /**
     * 升级开始时间
     */
    @Schema(description = "升级开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-08 07:30:00")
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    @Schema(description = "升级结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-08 07:30:00")
    private LocalDateTime endTime;

}
