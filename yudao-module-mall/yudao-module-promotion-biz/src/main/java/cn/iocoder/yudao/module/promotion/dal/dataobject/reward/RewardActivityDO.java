package cn.iocoder.yudao.module.promotion.dal.dataobject.reward;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 满减送活动 DO
 *
 * @author 芋道源码
 */
@TableName(value = "promotion_reward_activity", autoResultMap = true)
@KeySequence("promotion_reward_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class RewardActivityDO extends BaseDO {

    /**
     * 活动编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 活动标题
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link PromotionActivityStatusEnum}
     */
    private Integer status;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 条件类型
     *
     * 枚举 {@link PromotionConditionTypeEnum}
     */
    private Integer conditionType;
    /**
     * 商品范围
     *
     * 枚举 {@link PromotionProductScopeEnum}
     */
    private Integer productScope;
    /**
     * 商品 SPU 编号的数组
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> productSpuIds;
    /**
     * 优惠规则的数组
     */
    @TableField(typeHandler = RuleTypeHandler.class)
    private List<Rule> rules;

    /**
     * 优惠规则
     */
    @Data
    public static class Rule implements Serializable {

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

    // TODO @芋艿：可以找一些新的思路
    public static class RuleTypeHandler extends AbstractJsonTypeHandler<List<Rule>> {

        @Override
        protected List<Rule> parse(String json) {
            return JsonUtils.parseArray(json, Rule.class);
        }

        @Override
        protected String toJson(List<Rule> obj) {
            return JsonUtils.toJsonString(obj);
        }

    }

}
