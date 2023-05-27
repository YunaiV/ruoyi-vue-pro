package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ExpressTemplateChargeUpdateVO extends ExpressTemplateChargeBaseVO {

    @Schema(description = "编号", example = "6592")
    private Long id;

    @Schema(description = "配送模板编号", example = "1")
    private Long templateId;

    @Schema(description = "配送计费方式", example = "1")
    private Integer chargeMode;
}
