package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备属性 Response VO")
@Data
public class IotDeviceDataRespVO {

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identifier;

    @Schema(description = "最新值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object value;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    // ========== 基于 ThingModel 查询 ==========

//    @Schema(description = "物模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21816")
//    private Long thingModelId;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataType;

}