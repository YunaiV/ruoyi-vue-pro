package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 快递运费模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateUpdateReqVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "编号", required = true, example = "371")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "区域运费列表")
    @Valid
    private List<ExpressTemplateChargeUpdateVO> templateCharge = Collections.emptyList();

    @Schema(description = "包邮区域列表")
    @Valid
    private List<ExpressTemplateFreeUpdateVO> templateFree = Collections.emptyList();

}
