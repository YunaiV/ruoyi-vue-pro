package cn.iocoder.yudao.module.trade.controller.app.delivery.vo.express;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 快递公司 Response VO")
@Data
public class AppDeliveryExpressRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "顺丰")
    private String name;

}
