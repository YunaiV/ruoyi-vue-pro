package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 优惠劵模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponTemplateRespVO extends CouponTemplateBaseVO {

    @ApiModelProperty(value = "模板编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "状态", required = true, example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "领取优惠券的数量", required = true, example = "1024")
    private Integer takeCount;

    @ApiModelProperty(value = "使用优惠券的次数", required = true, example = "2048")
    private Integer useCount;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
