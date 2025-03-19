package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 产品物模型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotThinkModelFunctionPageReqVO extends PageParam {

    @Schema(description = "功能标识")
    private String identifier;

    @Schema(description = "功能名称", example = "张三")
    private String name;

    @Schema(description = "功能类型", example = "1")
    @InEnum(IotProductFunctionTypeEnum.class)
    private Integer type;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品ID不能为空")
    private Long productId;

}