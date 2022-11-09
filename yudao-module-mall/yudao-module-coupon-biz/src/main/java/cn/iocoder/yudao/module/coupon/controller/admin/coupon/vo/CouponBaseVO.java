package cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 优惠券 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class CouponBaseVO {

    @ApiModelProperty(value = "优惠券类型 reward-满减 discount-折扣 random-随机", required = true)
    @NotNull(message = "优惠券类型 reward-满减 discount-折扣 random-随机不能为空")
    private String type;

    @ApiModelProperty(value = "优惠券名称", required = true)
    @NotNull(message = "优惠券名称不能为空")
    private String name;

    @ApiModelProperty(value = "优惠券类型id")
    private Long couponTypeId;

    @ApiModelProperty(value = "优惠券编码", required = true)
    @NotNull(message = "优惠券编码不能为空")
    private String couponCode;

    @ApiModelProperty(value = "领用人", required = true)
    @NotNull(message = "领用人不能为空")
    private Long memberId;

    @ApiModelProperty(value = "优惠券使用订单id", required = true)
    @NotNull(message = "优惠券使用订单id不能为空")
    private Long useOrderId;

    @ApiModelProperty(value = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用", required = true)
    @NotNull(message = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用不能为空")
    private Boolean goodsType;

    @ApiModelProperty(value = "适用商品id", required = true)
    @NotNull(message = "适用商品id不能为空")
    private String goodsIds;

    @ApiModelProperty(value = "最小金额", required = true)
    @NotNull(message = "最小金额不能为空")
    private BigDecimal atLeast;

    @ApiModelProperty(value = "面额", required = true)
    @NotNull(message = "面额不能为空")
    private BigDecimal money;

    @ApiModelProperty(value = "1 =< 折扣 <= 9.9 当type为discount时需要添加", required = true)
    @NotNull(message = "1 =< 折扣 <= 9.9 当type为discount时需要添加不能为空")
    private BigDecimal discount;

    @ApiModelProperty(value = "最多折扣金额 当type为discount时可选择性添加", required = true)
    @NotNull(message = "最多折扣金额 当type为discount时可选择性添加不能为空")
    private BigDecimal discountLimit;

    @ApiModelProperty(value = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用", required = true)
    @NotNull(message = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用不能为空")
    private Boolean whetherForbidPreference;

    @ApiModelProperty(value = "是否开启过期提醒0-不开启 1-开启", required = true)
    @NotNull(message = "是否开启过期提醒0-不开启 1-开启")
    private Boolean whetherExpireNotice;

    @ApiModelProperty(value = "过期前N天提醒", required = true)
    @NotNull(message = "过期前N天提醒不能为空")
    private Integer expireNoticeFixedTerm;

    @ApiModelProperty(value = "是否已提醒", required = true)
    @NotNull(message = "是否已提醒不能为空")
    private Boolean whetherNoticed;

    @ApiModelProperty(value = "优惠券状态 1已领用（未使用） 2已使用 3已过期", required = true)
    @NotNull(message = "优惠券状态 1已领用（未使用） 2已使用 3已过期不能为空")
    private Integer state;

    @ApiModelProperty(value = "获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取", required = true)
    @NotNull(message = "获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取不能为空")
    private Boolean getType;

    @ApiModelProperty(value = "领取时间", required = true)
    @NotNull(message = "领取时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date fetchTime;

    @ApiModelProperty(value = "使用时间", required = true)
    @NotNull(message = "使用时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date useTime;

    @ApiModelProperty(value = "可使用的开始时间", required = true)
    @NotNull(message = "可使用的开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startTime;

    @ApiModelProperty(value = "有效期结束时间", required = true)
    @NotNull(message = "有效期结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

}
