package cn.iocoder.yudao.module.trade.service.order.bo;

import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO 芋艿：在想想这些参数的定义
/**
 * 订单创建之前 Request BO
 *
 * @author HUIHUI
 */
@Data
public class TradeBeforeOrderCreateReqBO {

    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    // ========== 秒杀活动相关字段 ==========

    /**
     *
     */
    @Schema(description = "秒杀活动编号", example = "1024")
    private Long seckillActivityId;

    // ========== 拼团活动相关字段 ==========

    /**
     * 拼团活动编号
     */
    @Schema(description = "拼团活动编号", example = "1024")
    private Long combinationActivityId;

    /**
     * 拼团团长编号
     */
    @Schema(description = "拼团团长编号", example = "2048")
    private Long combinationHeadId;

    // ========== 砍价活动相关字段 ==========

    /**
     * 砍价活动编号
     */
    @Schema(description = "砍价活动编号", example = "123")
    private Long bargainActivityId;

    // ========== 活动购买商品相关字段 ==========

    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSkuDO 的 spuId 编号
     */
    @NotNull(message = "SPU 编号不能为空")
    private Long spuId;

    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    @NotNull(message = "SKU 编号活动商品不能为空")
    private Long skuId;

    /**
     * 购买的商品数量
     */
    @NotNull(message = "购买数量不能为空")
    private Integer count;

}
