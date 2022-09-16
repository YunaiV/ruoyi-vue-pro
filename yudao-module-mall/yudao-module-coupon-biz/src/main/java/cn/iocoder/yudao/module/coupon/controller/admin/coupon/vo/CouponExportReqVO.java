package cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 优惠券 Excel 导出 Request VO", description = "参数和 CouponPageReqVO 是一致的")
@Data
public class CouponExportReqVO {

    @ApiModelProperty(value = "优惠券类型 reward-满减 discount-折扣 random-随机")
    private String type;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "优惠券类型id")
    private Long couponTypeId;

    @ApiModelProperty(value = "优惠券编码")
    private String couponCode;

    @ApiModelProperty(value = "领用人")
    private Long memberId;

    @ApiModelProperty(value = "优惠券使用订单id")
    private Long useOrderId;

    @ApiModelProperty(value = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用")
    private Boolean goodsType;

    @ApiModelProperty(value = "适用商品id")
    private String goodsIds;

    @ApiModelProperty(value = "最小金额")
    private BigDecimal atLeast;

    @ApiModelProperty(value = "面额")
    private BigDecimal money;

    @ApiModelProperty(value = "1 =< 折扣 <= 9.9 当type为discount时需要添加")
    private BigDecimal discount;

    @ApiModelProperty(value = "最多折扣金额 当type为discount时可选择性添加")
    private BigDecimal discountLimit;

    @ApiModelProperty(value = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用")
    private Boolean whetherForbidPreference;

    @ApiModelProperty(value = "是否开启过期提醒0-不开启 1-开启")
    private Boolean whetherExpireNotice;

    @ApiModelProperty(value = "过期前N天提醒")
    private Integer expireNoticeFixedTerm;

    @ApiModelProperty(value = "是否已提醒")
    private Boolean whetherNoticed;

    @ApiModelProperty(value = "优惠券状态 1已领用（未使用） 2已使用 3已过期")
    private Integer state;

    @ApiModelProperty(value = "获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取")
    private Boolean getType;

    @ApiModelProperty(value = "领取时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] fetchTime;

    @ApiModelProperty(value = "使用时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] useTime;

    @ApiModelProperty(value = "可使用的开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] startTime;

    @ApiModelProperty(value = "有效期结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] endTime;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
