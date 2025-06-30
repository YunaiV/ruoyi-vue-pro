package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理后台 - IoT OTA 升级任务分页 Request VO")
public class IotOtaUpgradeTaskPageReqVO extends PageParam {

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "固件编号不能为空")
    private Long firmwareId;

    @Schema(description = "任务名称", example = "升级任务")
    private String name;

}
