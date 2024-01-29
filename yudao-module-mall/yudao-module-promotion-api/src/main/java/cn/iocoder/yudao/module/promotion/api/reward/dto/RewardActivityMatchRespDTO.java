package cn.iocoder.yudao.module.promotion.api.reward.dto;

import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 满减送活动的匹配 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class RewardActivityMatchRespDTO {

    /**
     * 活动编号，主键自增
     */
    private Long id;
    /**
     * 活动标题
     */
    private String name;
    /**
     * 条件类型
     *
     * 枚举 {@link PromotionConditionTypeEnum}
     */
    private Integer conditionType;
    /**
     * 优惠规则的数组
     */
    private List<Rule> rules;

    /**
     * 商品 SPU 编号的数组
     */
    private List<Long> spuIds;

    // TODO 芋艿：后面 RewardActivityRespDTO 有了之后，Rule 可以放过去
    /**
     * 优惠规则
     */
    @Data
    public static class Rule {

        /**
         * 优惠门槛
         *
         * 1. 满 N 元，单位：分
         * 2. 满 N 件
         */
        private Integer limit;
        /**
         * 优惠价格，单位：分
         */
        private Integer discountPrice;
        /**
         * 是否包邮
         */
        private Boolean freeDelivery;
        /**
         * 赠送的积分
         */
        private Integer point;
        /**
         * 赠送的优惠劵编号的数组
         */
        private List<Long> couponIds;
        /**
         * 赠送的优惠券数量的数组
         */
        private List<Integer> couponCounts;

    }

}
