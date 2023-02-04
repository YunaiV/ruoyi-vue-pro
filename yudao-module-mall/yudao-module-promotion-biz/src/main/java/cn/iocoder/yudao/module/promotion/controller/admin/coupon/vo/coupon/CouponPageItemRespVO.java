package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 优惠劵分页的每一项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponPageItemRespVO extends CouponRespVO {

    @Schema(description = "用户昵称", example = "老芋艿")
    private String nickname;

}
