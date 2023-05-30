package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Schema(description = "管理后台 - 快递运费模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateUpdateReqVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "编号", required = true, example = "371")
    @NotNull(message = "编号不能为空")
    private Long id;

    // TODO @jason：pojo 不给默认值哈

    @Schema(description = "区域运费列表")
    @Valid
    private List<ExpressTemplateChargeUpdateVO> templateCharge = Collections.emptyList();

    @Schema(description = "包邮区域列表")
    @Valid
    private List<ExpressTemplateFreeUpdateVO> templateFree = Collections.emptyList();

}
