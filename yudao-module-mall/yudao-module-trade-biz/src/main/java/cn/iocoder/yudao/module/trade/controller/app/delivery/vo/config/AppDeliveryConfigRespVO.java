package cn.iocoder.yudao.module.trade.controller.app.delivery.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO 芋艿：后续要实现下，配送配置；后续融合到 AppTradeConfigRespVO 中
@Schema(description = "用户 App - 配送配置 Response VO")
@Data
public class AppDeliveryConfigRespVO {

    @Schema(description = "腾讯地图 KEY", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String tencentLbsKey;

    @Schema(description = "是否开启自提", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean pickUpEnable;

}
