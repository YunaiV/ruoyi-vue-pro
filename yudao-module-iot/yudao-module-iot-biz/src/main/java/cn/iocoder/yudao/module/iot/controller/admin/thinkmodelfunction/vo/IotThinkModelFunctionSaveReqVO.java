package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @Schema(description = "功能标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "功能标识不能为空")
    private String identifier;

    @Schema(description = "功能名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "功能名称不能为空")
    private String name;

    @Schema(description = "功能描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "功能类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "功能类型不能为空")
    @InEnum(IotProductFunctionTypeEnum.class)
    private Integer type;

    @Schema(description = "属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelProperty property;

    @Schema(description = "服务", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelService service;

    @Schema(description = "事件", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelEvent event;

}