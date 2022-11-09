package cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券模板 DO
 *
 * @author wxr
 */
@TableName("coupon_templete")
@KeySequence("coupon_templete_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTempleteDO extends BaseDO {

    /**
     * 用户ID
     */
    @TableId
    private Long id;
    /**
     * 优惠券类型 reward-满减 discount-折扣 random-随机
     */
    private String type;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 名称备注
     */
    private String couponNameRemark;
    /**
     * 优惠券图片
     */
    private String image;
    /**
     * 发放数量
     */
    private Integer count;
    /**
     * 已领取数量
     */
    private Integer leadCount;
    /**
     * 已使用数量
     */
    private Integer usedCount;
    /**
     * 适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用
     */
    private Integer goodsType;
    /**
     * 适用商品id
     */
    private String productIds;
    /**
     * 使用门槛0-无门槛 1-有门槛
     */
    private Boolean hasUseLimit;
    /**
     * 满多少元使用 0代表无限制
     */
    private BigDecimal atLeast;
    /**
     * 发放面额 当type为reward时需要添加
     */
    private BigDecimal money;
    /**
     * 1 =< 折扣 <= 9.9 当type为discount时需要添加
     */
    private BigDecimal discount;
    /**
     * 最多折扣金额 当type为discount时可选择性添加
     */
    private BigDecimal discountLimit;
    /**
     * 最低金额 当type为radom时需要添加
     */
    private BigDecimal minMoney;
    /**
     * 最大金额 当type为radom时需要添加
     */
    private BigDecimal maxMoney;
    /**
     * 过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期
     */
    private Integer validityType;
    /**
     * 使用开始日期 过期类型1时必填
     */
    private Date startUseTime;
    /**
     * 使用结束日期 过期类型1时必填
     */
    private Date endUseTime;
    /**
     * 当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效
     */
    private Integer fixedTerm;
    /**
     * 是否无限制0-否 1是
     */
    private Boolean whetherLimitless;
    /**
     * 每人最大领取个数
     */
    private Integer maxFetch;
    /**
     * 是否开启过期提醒0-不开启 1-开启
     */
    private Boolean whetherExpireNotice;
    /**
     * 过期前N天提醒
     */
    private Integer expireNoticeFixedTerm;
    /**
     * 优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用
     */
    private Boolean whetherForbidPreference;
    /**
     * 是否显示
     */
    private Integer whetherShow;
    /**
     * 订单的优惠总金额
     */
    private BigDecimal discountOrderMoney;
    /**
     * 用券总成交额
     */
    private BigDecimal orderMoney;
    /**
     * 是否禁止发放0-否 1-是
     */
    private Boolean whetherForbidden;
    /**
     * 使用优惠券购买的商品数量
     */
    private Integer orderGoodsNum;
    /**
     * 状态（1进行中2已结束-1已关闭）
     */
    private Integer status;
    /**
     * 有效日期结束时间
     */
    private Date endTime;

}
