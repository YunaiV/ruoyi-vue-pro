package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 快递运费模板包邮 Base VO，提供给添加运费模板使用
 */
@Data
public class DeliveryExpressTemplateFreeBaseVO {

    @Schema(description = "区域编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,120000]")
    @NotEmpty(message = "区域编号列表不能为空")
    private List<Integer> areaIds;

    @Schema(description = "包邮金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "包邮金额不能为空")
    private Integer freePrice;

    @Schema(description = "包邮件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "包邮件数不能为空")
    private Integer freeCount;

}
