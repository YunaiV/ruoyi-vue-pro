package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 拼团记录的创建 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationRecordCreateReqDTO {

    /**
     * 拼团活动编号
     */
    @NotNull(message = "拼团活动编号不能为空")
    private Long activityId;
    /**
     * spu 编号
     */
    @NotNull(message = "spu 编号不能为空")
    private Long spuId;
    /**
     * sku 编号
     */
    @NotNull(message = "sku 编号不能为空")
    private Long skuId;
    /**
     * 购买的商品数量
     */
    @NotNull(message = "购买数量不能为空")
    private Integer count;
    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 团长编号
     */
    private Long headId;
    /**
     * 拼团商品单价
     */
    @NotNull(message = "拼团商品单价不能为空")
    private Integer combinationPrice;

}
