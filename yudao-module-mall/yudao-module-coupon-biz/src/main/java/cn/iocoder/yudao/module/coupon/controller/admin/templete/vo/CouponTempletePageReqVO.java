package cn.iocoder.yudao.module.coupon.controller.admin.templete.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 优惠券模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponTempletePageReqVO extends PageParam {

    @ApiModelProperty(value = "优惠券类型 reward-满减 discount-折扣 random-随机")
    private String type;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "名称备注")
    private String couponNameRemark;

    @ApiModelProperty(value = "优惠券图片")
    private String image;

    @ApiModelProperty(value = "发放数量")
    private Integer count;

    @ApiModelProperty(value = "已领取数量")
    private Integer leadCount;

    @ApiModelProperty(value = "已使用数量")
    private Integer usedCount;

    @ApiModelProperty(value = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用")
    private Boolean goodsType;

    @ApiModelProperty(value = "适用商品id")
    private String productIds;

    @ApiModelProperty(value = "使用门槛0-无门槛 1-有门槛")
    private Boolean hasUseLimit;

    @ApiModelProperty(value = "满多少元使用 0代表无限制")
    private BigDecimal atLeast;

    @ApiModelProperty(value = "发放面额 当type为reward时需要添加")
    private BigDecimal money;

    @ApiModelProperty(value = "1 =< 折扣 <= 9.9 当type为discount时需要添加")
    private BigDecimal discount;

    @ApiModelProperty(value = "最多折扣金额 当type为discount时可选择性添加")
    private BigDecimal discountLimit;

    @ApiModelProperty(value = "最低金额 当type为radom时需要添加")
    private BigDecimal minMoney;

    @ApiModelProperty(value = "最大金额 当type为radom时需要添加")
    private BigDecimal maxMoney;

    @ApiModelProperty(value = "过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期")
    private Boolean validityType;

    @ApiModelProperty(value = "使用开始日期 过期类型1时必填")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] startUseTime;

    @ApiModelProperty(value = "使用结束日期 过期类型1时必填")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] endUseTime;

    @ApiModelProperty(value = "当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效")
    private Integer fixedTerm;

    @ApiModelProperty(value = "是否无限制0-否 1是")
    private Boolean whetherLimitless;

    @ApiModelProperty(value = "每人最大领取个数")
    private Integer maxFetch;

    @ApiModelProperty(value = "是否开启过期提醒0-不开启 1-开启")
    private Boolean whetherExpireNotice;

    @ApiModelProperty(value = "过期前N天提醒")
    private Integer expireNoticeFixedTerm;

    @ApiModelProperty(value = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用")
    private Boolean whetherForbidPreference;

    @ApiModelProperty(value = "是否显示")
    private Integer whetherShow;

    @ApiModelProperty(value = "订单的优惠总金额")
    private BigDecimal discountOrderMoney;

    @ApiModelProperty(value = "用券总成交额")
    private BigDecimal orderMoney;

    @ApiModelProperty(value = "是否禁止发放0-否 1-是")
    private Boolean whetherForbidden;

    @ApiModelProperty(value = "使用优惠券购买的商品数量")
    private Integer orderGoodsNum;

    @ApiModelProperty(value = "状态（1进行中2已结束-1已关闭）")
    private Integer status;

    @ApiModelProperty(value = "有效日期结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] endTime;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
