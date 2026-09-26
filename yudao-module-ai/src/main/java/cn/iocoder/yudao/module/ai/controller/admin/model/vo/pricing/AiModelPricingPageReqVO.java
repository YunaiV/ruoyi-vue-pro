package cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 模型计费配置分页 Request VO")
@Data
public class AiModelPricingPageReqVO extends PageParam {

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
