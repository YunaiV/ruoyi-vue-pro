package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 优惠劵模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponTemplateRespVO extends CouponTemplateBaseVO {

    @Schema(title = "模板编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "状态", required = true, example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(title = "领取优惠券的数量", required = true, example = "1024")
    private Integer takeCount;

    @Schema(title = "使用优惠券的次数", required = true, example = "2048")
    private Integer useCount;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
