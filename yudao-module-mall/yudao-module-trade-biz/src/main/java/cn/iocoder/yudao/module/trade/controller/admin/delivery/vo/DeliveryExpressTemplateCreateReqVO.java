package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;

@Schema(description = "管理后台 - 快递运费模板创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateCreateReqVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "区域运费列表")
    @Valid
    private List<ExpressTemplateChargeBaseVO> templateCharge = Collections.emptyList();

    @Schema(description = "包邮区域列表")
    @Valid
    private List<ExpressTemplateFreeBaseVO> templateFree = Collections.emptyList();

}
