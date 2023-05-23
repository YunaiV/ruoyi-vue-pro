package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 快递运费模板运费设置 Base VO，提供给添加运费模板使用
 *
 * @author jason
 */
@Data
public class ExpressTemplateChargeBaseVO {

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "区域编号不能为空")
    private Integer areaId;

    @Schema(description = "首件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "首件数量不能为空")
    private Double startCount;

    @Schema(description = "起步价", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "起步价不能为空")
    private Integer startPrice;

    @Schema(description = "续件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "续件数量不能为空")
    private Double extraCount;

    @Schema(description = "额外价", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    @NotNull(message = "额外价不能为空")
    private Integer extraPrice;
}
