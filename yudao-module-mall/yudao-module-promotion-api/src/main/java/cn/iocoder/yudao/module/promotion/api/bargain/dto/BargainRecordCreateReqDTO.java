package cn.iocoder.yudao.module.promotion.api.bargain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO @芋艿：这块要在看看

/**
 * 砍价记录的创建 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class BargainRecordCreateReqDTO {

    /**
     * 砍价活动编号
     */
    @NotNull(message = "砍价活动编号不能为空")
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
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    /**
     * 砍价商品单价
     */
    @NotNull(message = "砍价底价不能为空")
    private Integer bargainPrice;
    /**
     * 商品原价，单位分
     */
    @NotNull(message = "商品原价不能为空")
    private Integer price;

    /**
     * 开团状态：进行中 砍价成功 砍价失败
     */
    @NotNull(message = "开团状态不能为空")
    private Integer status;

}
