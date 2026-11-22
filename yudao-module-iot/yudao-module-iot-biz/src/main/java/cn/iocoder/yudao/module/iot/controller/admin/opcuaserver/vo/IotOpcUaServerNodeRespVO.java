package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT OPC UA Server 节点 Response VO")
@Data
@Accessors(chain = true)
public class IotOpcUaServerNodeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long configId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "plc-01")
    private String deviceName;

    @Schema(description = "物模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long thingModelId;

    @Schema(description = "物模型属性标识符", example = "temperature")
    private String identifier;

    @Schema(description = "物模型属性名称", example = "温度")
    private String name;

    @Schema(description = "物模型属性是否有效（已删除、不再是属性或不属于设备所属产品时为 false）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean thingModelValid;

    @Schema(description = "浏览名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "line1.temperature")
    private String browseName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
