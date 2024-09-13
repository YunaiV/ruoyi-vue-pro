package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 产品物模型新增/修改 Request VO")
@Data
public class IotThinkModelFunctionSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "属性列表不能为空")
    private List<ThingModelProperty> properties;

    @Schema(description = "服务列表")
    private List<ThingModelService> services;

    @Schema(description = "事件列表")
    private List<ThingModelEvent> events;

}