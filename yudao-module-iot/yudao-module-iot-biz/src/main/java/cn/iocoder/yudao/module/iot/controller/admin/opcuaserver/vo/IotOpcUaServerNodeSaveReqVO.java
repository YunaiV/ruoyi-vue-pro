package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - IoT OPC UA Server 节点新增/修改 Request VO")
@Data
public class IotOpcUaServerNodeSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "配置编号不能为空")
    private Long configId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型编号（设备所属产品的属性）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "物模型编号不能为空")
    private Long thingModelId;

    @Schema(description = "浏览名称（同一配置下唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "line1.temperature")
    @NotEmpty(message = "浏览名称不能为空")
    @Size(max = 128, message = "浏览名称长度不能超过 128 个字符")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "浏览名称只能包含字母、数字、下划线、点和中划线")
    private String browseName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
