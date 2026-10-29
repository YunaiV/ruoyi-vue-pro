package cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - IoT 设备影子更新期望状态 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备影子更新期望状态 Request VO")
@Data
public class IotDeviceShadowUpdateDesiredReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "期望状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "期望状态不能为空")
    private Map<String, Object> desired;

}
