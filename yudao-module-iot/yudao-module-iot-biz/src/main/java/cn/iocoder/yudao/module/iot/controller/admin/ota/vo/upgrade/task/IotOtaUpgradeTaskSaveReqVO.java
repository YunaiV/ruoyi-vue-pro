package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "管理后台 - IoT OTA 升级任务创建/修改 Request VO")
public class IotOtaUpgradeTaskSaveReqVO {

    // TODO @li：已经有注解，不用重复注释
    // TODO @li： @Schema 写在参数校验前面。先有定义；其他的，也检查下；

    /**
     * 任务名称
     */
    @NotEmpty(message = "任务名称不能为空")
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
    @NotNull(message = "固件编号不能为空")
    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;

    /**
     * 升级范围
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum}
     */
    @NotNull(message = "升级范围不能为空")
    @InEnum(value = IotOtaUpgradeTaskScopeEnum.class)
    @Schema(description = "升级范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer scope;

    /**
     * 选中的设备编号数组
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    @Schema(description = "选中的设备编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3,4]")
    private List<Long> deviceIds;

}
