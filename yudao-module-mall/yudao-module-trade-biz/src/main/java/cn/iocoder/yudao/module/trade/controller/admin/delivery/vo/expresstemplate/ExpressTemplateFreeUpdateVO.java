package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @jason：这个 vo 可以内嵌到 DeliveryExpressTemplateUpdateReqVO，避免 vo 过多，不好分辨
// TODO @jason：swagger 缺失
/**
 * 快递运费模板包邮 更新 VO
 */
@Data
public class ExpressTemplateFreeUpdateVO extends ExpressTemplateFreeBaseVO {

    @Schema(description = "编号", example = "6592")
    private Long id;

    @Schema(description = "配送模板编号", example = "1")
    private Long templateId;
}
