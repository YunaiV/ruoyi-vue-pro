package cn.iocoder.yudao.module.coupon.controller.admin.templete.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 优惠券模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponTempleteUpdateReqVO extends CouponTempleteBaseVO {

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long id;

}
