package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理后台 - IoT OTA 升级任务 Response VO")
public class IotOtaUpgradeTaskRespVO implements VO {

    /**
     * 任务编号
     */
    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;
    /**
     * 任务名称
     */
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "升级任务")
    private String name;
    /**
     * 任务描述
     */
    @Schema(description = "任务描述", example = "升级任务")
    private String description;
    /**
     * 固件编号
     * <p>
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;
    /**
     * 任务状态
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskStatusEnum}
     */
    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"10", "20", "21", "30"})
    private Integer status;
    /**
     * 任务状态名称
     */
    @Schema(description = "任务状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "进行中")
    private String statusName;
    /**
     * 升级范围
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum}
     */
    @Schema(description = "升级范围", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2"})
    private Integer scope;
    /**
     * 设备数量
     */
    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceCount;
    /**
     * 选中的设备编号数组
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    @Schema(description = "选中的设备编号数组", example = "1024")
    private List<Long> deviceIds;
    /**
     * 选中的设备名字数组
     * <p>
     * 关联 {@link IotDeviceDO#getDeviceName()}
     */
    @Schema(description = "选中的设备名字数组", example = "1024")
    private List<String> deviceNames;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-08 07:30:00")
    private LocalDateTime createTime;

}
