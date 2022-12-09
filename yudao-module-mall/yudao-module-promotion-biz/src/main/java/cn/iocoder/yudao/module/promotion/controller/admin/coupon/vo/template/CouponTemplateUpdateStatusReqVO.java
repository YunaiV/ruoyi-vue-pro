package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(title = "管理后台 - 优惠劵模板更新状态 Request VO")
@Data
public class CouponTemplateUpdateStatusReqVO {

    @Schema(title = "优惠劵模板编号", required = true, example = "1024")
    @NotNull(message = "优惠劵模板编号不能为空")
    private Long id;

    @Schema(title = "状态", required = true, example = "1", description = "见 CommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;

}
