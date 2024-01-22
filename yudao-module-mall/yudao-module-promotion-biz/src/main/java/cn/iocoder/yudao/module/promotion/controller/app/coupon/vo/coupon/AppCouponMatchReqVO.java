package cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 优惠劵的匹配 Request VO")
@Data
public class AppCouponMatchReqVO {

    @Schema(description = "商品金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品金额不能为空")
    private Integer price;

    @Schema(description = "商品 SPU 编号的数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "商品 SPU 编号不能为空")
    private List<Long> spuIds;

    @Schema(description = "商品 SKU 编号的数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "商品 SKU 编号不能为空")
    private List<Long> skuIds;

    @Schema(description = "分类编号的数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[10, 20]")
    @NotEmpty(message = "分类编号不能为空")
    private List<Long> categoryIds;

}
