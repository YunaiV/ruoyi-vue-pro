package cn.iocoder.yudao.module.coupon.dal.dataobject.coupon;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 优惠券 DO
 *
 * @author wxr
 */
@TableName("coupon")
@KeySequence("coupon_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDO extends BaseDO {

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
     * 优惠券类型id
     */
    private Long couponTypeId;
    /**
     * 优惠券编码
     */
    private String couponCode;
    /**
     * 领用人
     */
    private Long memberId;
    /**
     * 优惠券使用订单id
     */
    private Long useOrderId;
    /**
     * 适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用
     */
    private Boolean goodsType;
    /**
     * 适用商品id
     */
    private String goodsIds;
    /**
     * 最小金额
     */
    private BigDecimal atLeast;
    /**
     * 面额
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
     * 优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用
     */
    private Boolean whetherForbidPreference;
    /**
     * 是否开启过期提醒0-不开启 1-开启
     */
    private Boolean whetherExpireNotice;
    /**
     * 过期前N天提醒
     */
    private Integer expireNoticeFixedTerm;
    /**
     * 是否已提醒
     */
    private Boolean whetherNoticed;
    /**
     * 优惠券状态 1已领用（未使用） 2已使用 3已过期
     */
    private Integer state;
    /**
     * 获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取
     */
    private Boolean getType;
    /**
     * 领取时间
     */
    private Date fetchTime;
    /**
     * 使用时间
     */
    private Date useTime;
    /**
     * 可使用的开始时间
     */
    private Date startTime;
    /**
     * 有效期结束时间
     */
    private Date endTime;

}
