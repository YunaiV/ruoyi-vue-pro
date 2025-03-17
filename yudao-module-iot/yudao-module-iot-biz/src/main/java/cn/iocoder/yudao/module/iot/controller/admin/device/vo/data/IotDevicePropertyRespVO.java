package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备属性 Response VO")
@Data
public class IotDevicePropertyRespVO {

    @Schema(description = "属性定义", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelProperty property;

    @Schema(description = "最新值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object value;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime; // 由于从 TDengine 查询出来的是 Long 类型，所以这里也使用 Long 类型

}