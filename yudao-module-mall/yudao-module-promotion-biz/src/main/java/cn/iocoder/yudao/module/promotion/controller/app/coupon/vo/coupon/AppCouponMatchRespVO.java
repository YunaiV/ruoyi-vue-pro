package cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 优惠劵 Response VO")
@Data
public class AppCouponMatchRespVO extends AppCouponRespVO {

    @Schema(description = "是否匹配", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean match;

    @Schema(description = "匹配条件的提示", example = "所结算商品没有符合条件的商品")
    private String description;

}
