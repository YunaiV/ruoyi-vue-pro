package cn.iocoder.yudao.module.promotion.controller.admin.discount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 限时折扣活动的详细 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiscountActivityDetailRespVO extends DiscountActivityRespVO {

    /**
     * 商品列表
     */
    private List<Product> products;

}
