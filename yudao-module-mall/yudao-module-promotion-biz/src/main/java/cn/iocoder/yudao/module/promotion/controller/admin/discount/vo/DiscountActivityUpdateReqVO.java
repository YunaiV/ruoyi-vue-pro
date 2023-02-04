package cn.iocoder.yudao.module.promotion.controller.admin.discount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 限时折扣活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiscountActivityUpdateReqVO extends DiscountActivityBaseVO {

    @Schema(description = "活动编号", required = true, example = "1024")
    @NotNull(message = "活动编号不能为空")
    private Long id;

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<DiscountActivityCreateReqVO.Product> products;

}
