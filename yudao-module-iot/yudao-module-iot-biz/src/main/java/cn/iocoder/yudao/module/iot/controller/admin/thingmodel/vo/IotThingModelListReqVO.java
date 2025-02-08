package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品物模型 List Request VO")
@Data
public class IotThingModelListReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @Schema(description = "功能标识", example = "temperature")
    private String identifier;

    @Schema(description = "功能名称", example = "温度")
    private String name;

    @Schema(description = "功能类型", example = "1")
    @InEnum(IotThingModelTypeEnum.class)
    private Integer type;

}
