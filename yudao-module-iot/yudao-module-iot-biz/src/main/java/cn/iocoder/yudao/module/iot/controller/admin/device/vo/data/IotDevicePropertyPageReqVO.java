package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备属性 Request VO")
@Data
public class IotDevicePropertyPageReqVO extends PageParam {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "设备 Key", hidden = true)
    private String deviceKey; // 非前端传递，后端自己查询设置

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identifier;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

}