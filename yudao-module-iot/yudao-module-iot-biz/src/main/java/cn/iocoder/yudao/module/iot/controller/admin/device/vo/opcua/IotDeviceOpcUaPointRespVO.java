package cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备 OPC UA 点位配置 Response VO")
@Data
public class IotDeviceOpcUaPointRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long thingModelId;

    @Schema(description = "属性标识符（关联物模型；物模型失效时为空）", example = "temperature")
    private String identifier;

    @Schema(description = "属性名称（关联物模型；物模型失效时为空）", example = "温度")
    private String name;

    @Schema(description = "物模型属性是否有效（已删除或不再是属性时为 false，网关跳过该点位）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean thingModelValid;

    @Schema(description = "NodeId", requiredMode = Schema.RequiredMode.REQUIRED, example = "nsu=urn:example;s=Temperature")
    private String nodeId;

    @Schema(description = "采集方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer collectMode;

    @Schema(description = "采集间隔（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    private Integer collectInterval;

    @Schema(description = "是否可写", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean writable;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
