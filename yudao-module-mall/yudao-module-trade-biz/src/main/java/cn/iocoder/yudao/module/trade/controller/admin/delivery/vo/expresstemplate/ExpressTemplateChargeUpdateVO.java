package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @jason：这个 vo 可以内嵌到 DeliveryExpressTemplateUpdateReqVO，避免 vo 过多，不好分辨
@Schema(description = "管理后台 - 快递公司创建 Request VO")
@Data
public class ExpressTemplateChargeUpdateVO extends ExpressTemplateChargeBaseVO {

    @Schema(description = "编号", example = "6592")
    private Long id;

    // TODO @jason：这几个字段，应该不通过前端传递，而是后端查询后去赋值的

    @Schema(description = "配送模板编号", example = "1")
    private Long templateId;

    @Schema(description = "配送计费方式", example = "1")
    private Integer chargeMode;

}
