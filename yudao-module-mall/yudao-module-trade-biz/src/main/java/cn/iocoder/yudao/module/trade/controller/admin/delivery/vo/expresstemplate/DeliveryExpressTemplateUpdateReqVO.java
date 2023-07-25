package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    private List<ExpressTemplateChargeUpdateVO> templateCharge;

    @Schema(description = "包邮区域列表")
    @Valid
    private List<ExpressTemplateFreeUpdateVO> templateFree;

    @Schema(description = "管理后台 - 快递运费模板区域运费更新 Request VO")
    @Data
    public static class ExpressTemplateChargeUpdateVO extends ExpressTemplateChargeBaseVO {

        @Schema(description = "编号", example = "6592")
        private Long id;

        // TODO @jason：这几个字段，应该不通过前端传递，而是后端查询后去赋值的
        @Schema(description = "配送模板编号", example = "1")
        private Long templateId;

        @Schema(description = "配送计费方式", example = "1")
        private Integer chargeMode;

    }

    @Schema(description = "管理后台 - 快递运费模板包邮区域更新 Request VO")
    @Data
    public static class ExpressTemplateFreeUpdateVO extends ExpressTemplateFreeBaseVO {

        @Schema(description = "编号", example = "6592")
        private Long id;

        @Schema(description = "配送模板编号", example = "1")
        private Long templateId;
    }
}
