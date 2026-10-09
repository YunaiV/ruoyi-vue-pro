package cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 模型计费配置新增/修改 Request VO")
@Data
public class AiModelPricingSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型编号不能为空")
    private Long modelId;

    @Schema(description = "输入单价（元/100万tokens，缓存未命中）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.0")
    @NotNull(message = "输入单价不能为空")
    @DecimalMin(value = "0", message = "输入单价不能小于 0")
    private Double priceInPer1mYuan;

    @Schema(description = "缓存命中输入单价（元/100万tokens），0或不填表示不区分", example = "0.5")
    @DecimalMin(value = "0", message = "缓存命中输入单价不能小于 0")
    private Double priceCachedPer1mYuan;

    @Schema(description = "输出单价（元/100万tokens，标准输出）", requiredMode = Schema.RequiredMode.REQUIRED, example = "8.0")
    @NotNull(message = "输出单价不能为空")
    @DecimalMin(value = "0", message = "输出单价不能小于 0")
    private Double priceOutPer1mYuan;

    @Schema(description = "推理/思考输出单价（元/100万tokens），0或不填表示不区分", example = "16.0")
    @DecimalMin(value = "0", message = "推理/思考输出单价不能小于 0")
    private Double priceReasoningPer1mYuan;

    @Schema(description = "计费策略类型", example = "DEFAULT")
    private String strategyType;

    @Schema(description = "策略扩展配置（JSON）", example = "{}")
    private String strategyConfig;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
