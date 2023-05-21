package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 快递运费模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateRespVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "371")
    private Long id;

    @Schema(description = "运费模板运费设置", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ExpressTemplateChargeBaseVO> templateCharge;

    @Schema(description = "运费模板包邮区域", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ExpressTemplateFreeBaseVO> templateFree;

}
