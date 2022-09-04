package cn.iocoder.yudao.module.coupon.controller.admin.templete.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 优惠券模板 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class CouponTempleteBaseVO {


    //基本信息

    @ApiModelProperty(value = "优惠券名称", required = true)
    @NotNull(message = "优惠券名称不能为空")
    private String name;

    @ApiModelProperty(value = "名称备注")
    private String couponNameRemark;

    @ApiModelProperty(value = "优惠券图片")
    private String image;

    /*  ============判断适用商品——开始=============  */


    @ApiModelProperty(value = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用", required = true)
    @NotNull(message = "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用不能为空")
    private Integer goodsType;

    @ApiModelProperty(value = "适用商品id")
    private String productIds;

    @ApiModelProperty(value = "使用门槛0-无门槛 1-有门槛", required = true)
    @NotNull(message = "使用门槛0-无门槛 1-有门槛不能为空")
    private Boolean hasUseLimit;

    @ApiModelProperty(value = "满多少元使用 0代表无限制", required = true)
    @NotNull(message = "满多少元使用 0代表无限制不能为空")
    private BigDecimal atLeast;


    /*  ============折扣类型——开始=============  */

    @ApiModelProperty(value = "优惠券类型 reward-满减 discount-折扣 random-随机", required = true)
    @NotNull(message = "优惠券类型 reward-满减 discount-折扣 random-随机不能为空")
    private String type;

    @ApiModelProperty(value = "发放面额 当type为reward时需要添加")
    @NotNull(message = "发放面额 当type为reward时需要添加不能为空")
    private BigDecimal money;

    @ApiModelProperty(value = "1 =< 折扣 <= 9.9 当type为discount时需要添加")
    @NotNull(message = "1 =< 折扣 <= 9.9 当type为discount时需要添加不能为空")
    private BigDecimal discount;

    @ApiModelProperty(value = "最多折扣金额 当type为discount时可选择性添加")
    @NotNull(message = "最多折扣金额 当type为discount时可选择性添加不能为空")
    private BigDecimal discountLimit;

    @ApiModelProperty(value = "最低金额 当type为radom时需要添加", required = true)
    @NotNull(message = "最低金额 当type为radom时需要添加不能为空")
    private BigDecimal minMoney;

    @ApiModelProperty(value = "最大金额 当type为radom时需要添加", required = true)
    @NotNull(message = "最大金额 当type为radom时需要添加不能为空")
    private BigDecimal maxMoney;

    /*  ============折扣类型——结束=============  */


    /*  ============过期类型——开始=============  */


    @ApiModelProperty(value = "过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期", required = true)
    @NotNull(message = "过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期 不能为空")
    private Integer validityType;

    @ApiModelProperty(value = "使用开始日期 过期类型1时必填")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startUseTime;

    @ApiModelProperty(value = "使用结束日期 过期类型1时必填")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endUseTime;

    @ApiModelProperty(value = "当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效")
    @NotNull(message = "当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效不能为空")
    private Integer fixedTerm;

    @ApiModelProperty(value = "有效日期结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

    /*  ============过期类型——结束=============  */


    @ApiModelProperty(value = "领取是否无限制0-否 1是", required = true)
    @NotNull(message = "是否无限制0-否 1是")
    private Boolean whetherLimitless;

    @ApiModelProperty(value = "每人最大领取个数", required = true)
    @NotNull(message = "每人最大领取个数不能为空")
    private Integer maxFetch;

    @ApiModelProperty(value = "是否开启过期提醒 0-不开启 1-开启", required = true)
    @NotNull(message = "是否开启过期提醒0-不开启 1-开启不能为空")
    private Boolean whetherExpireNotice;

    @ApiModelProperty(value = "过期前N天提醒", required = true)
    @NotNull(message = "过期前N天提醒不能为空")
    private Integer expireNoticeFixedTerm;

    @ApiModelProperty(value = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用", required = true)
    @NotNull(message = "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用不能为空")
    private Boolean whetherForbidPreference;

    @ApiModelProperty(value = "是否显示", required = true)
    @NotNull(message = "是否显示不能为空")
    private Integer whetherShow;

    @ApiModelProperty(value = "是否禁止发放0-否 1-是", required = true)
    @NotNull(message = "是否禁止发放0-否 1-是不能为空")
    private Boolean whetherForbidden;

    /*  ============汇总计算——开始=============  */



    @ApiModelProperty(value = "使用优惠券购买的商品数量", required = true)
    @NotNull(message = "使用优惠券购买的商品数量不能为空")
    private Integer orderGoodsNum;

    @ApiModelProperty(value = "订单的优惠总金额", required = true)
    @NotNull(message = "订单的优惠总金额不能为空")
    private BigDecimal discountOrderMoney;

    @ApiModelProperty(value = "用券总成交额", required = true)
    @NotNull(message = "用券总成交额不能为空")
    private BigDecimal orderMoney;

    @ApiModelProperty(value = "发放数量", required = true)
    @NotNull(message = "发放数量不能为空")
    private Integer count;

    @ApiModelProperty(value = "已领取数量", required = true)
    @NotNull(message = "已领取数量不能为空")
    private Integer leadCount;

    @ApiModelProperty(value = "已使用数量", required = true)
    @NotNull(message = "已使用数量不能为空")
    private Integer usedCount;


    /*  ============汇总计算——结束=============  */


    @ApiModelProperty(value = "状态（1进行中2已结束3已关闭）", required = true)
    @NotNull(message = "状态（1进行中2已结束-1已关闭）不能为空")
    private Integer status;



}
