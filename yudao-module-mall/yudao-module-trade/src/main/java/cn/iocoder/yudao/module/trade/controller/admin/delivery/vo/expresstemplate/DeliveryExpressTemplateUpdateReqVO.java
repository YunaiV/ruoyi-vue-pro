package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 快递运费模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateUpdateReqVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "371")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "区域运费列表")
    @Valid
    private List<DeliveryExpressTemplateChargeBaseVO> charges;

    @Schema(description = "包邮区域列表")
    @Valid
    private List<DeliveryExpressTemplateFreeBaseVO> frees;

}
