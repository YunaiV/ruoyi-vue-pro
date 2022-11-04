package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 优惠劵模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponTemplateUpdateReqVO extends CouponTemplateBaseVO {

    @ApiModelProperty(value = "模板编号", required = true, example = "1024")
    @NotNull(message = "模板编号不能为空")
    private Long id;

}
