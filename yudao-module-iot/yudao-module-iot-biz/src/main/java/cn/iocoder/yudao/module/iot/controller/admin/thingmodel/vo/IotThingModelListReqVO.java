package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品物模型List Request VO")
@Data
public class IotThingModelListReqVO {

    @Schema(description = "功能标识")
    private String identifier;

    @Schema(description = "功能名称", example = "张三")
    private String name;

    @Schema(description = "功能类型", example = "1")
    @InEnum(IotThingModelTypeEnum.class)
    private Integer type;

    @Schema(description = "产品 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品 ID 不能为空")
    private Long productId;

}
