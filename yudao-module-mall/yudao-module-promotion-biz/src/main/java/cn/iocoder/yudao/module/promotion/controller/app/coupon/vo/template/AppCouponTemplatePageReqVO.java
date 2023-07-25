package cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 优惠劵模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCouponTemplatePageReqVO extends PageParam {

    @Schema(description = "使用类型", example = "1")
    // TODO 芋艿：这里要限制下枚举的使用
    private Integer useType;

}
