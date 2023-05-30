package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Schema(description = "管理后台 - 快递运费模板创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateCreateReqVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "区域运费列表")
    @Valid
    private List<ExpressTemplateChargeBaseVO> templateCharge;

    @Schema(description = "包邮区域列表")
    @Valid
    private List<ExpressTemplateFreeBaseVO> templateFree;

}
