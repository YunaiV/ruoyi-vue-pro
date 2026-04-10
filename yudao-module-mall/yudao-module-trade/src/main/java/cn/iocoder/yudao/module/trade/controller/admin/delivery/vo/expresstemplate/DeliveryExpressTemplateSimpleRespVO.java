package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "管理后台 - 模版精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryExpressTemplateSimpleRespVO {

    @Schema(description = "模版编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试模版")
    private String name;

}
