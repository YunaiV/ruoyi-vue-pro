package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 快递运费模板包邮 更新 VO
 *
 * @author jason
 */
@Data
public class ExpressTemplateFreeUpdateVO extends ExpressTemplateFreeBaseVO {

    @Schema(description = "编号", example = "6592")
    private Long id;

    @Schema(description = "配送模板编号", example = "1")
    private Long templateId;
}
