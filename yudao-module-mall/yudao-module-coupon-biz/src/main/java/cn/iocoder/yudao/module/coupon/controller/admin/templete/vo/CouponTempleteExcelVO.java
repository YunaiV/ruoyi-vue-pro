package cn.iocoder.yudao.module.coupon.controller.admin.templete.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 优惠券模板 Excel VO
 *
 * @author wxr
 */
@Data
public class CouponTempleteExcelVO {

    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("优惠券类型 reward-满减 discount-折扣 random-随机")
    private String type;

    @ExcelProperty("优惠券名称")
    private String name;

    @ExcelProperty("名称备注")
    private String couponNameRemark;

    @ExcelProperty("优惠券图片")
    private String image;

    @ExcelProperty("发放数量")
    private Integer count;

    @ExcelProperty("已领取数量")
    private Integer leadCount;

    @ExcelProperty("已使用数量")
    private Integer usedCount;

    @ExcelProperty("适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用")
    private Integer goodsType;

    @ExcelProperty("适用商品id")
    private String productIds;

    @ExcelProperty("使用门槛0-无门槛 1-有门槛")
    private Boolean hasUseLimit;

    @ExcelProperty("满多少元使用 0代表无限制")
    private BigDecimal atLeast;

    @ExcelProperty("发放面额 当type为reward时需要添加")
    private BigDecimal money;

    @ExcelProperty("1 =< 折扣 <= 9.9 当type为discount时需要添加")
    private BigDecimal discount;

    @ExcelProperty("最多折扣金额 当type为discount时可选择性添加")
    private BigDecimal discountLimit;

    @ExcelProperty("最低金额 当type为radom时需要添加")
    private BigDecimal minMoney;

    @ExcelProperty("最大金额 当type为radom时需要添加")
    private BigDecimal maxMoney;

    @ExcelProperty("过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期")
    private Integer validityType;

    @ExcelProperty("使用开始日期 过期类型1时必填")
    private Date startUseTime;

    @ExcelProperty("使用结束日期 过期类型1时必填")
    private Date endUseTime;

    @ExcelProperty("当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效")
    private Integer fixedTerm;

    @ExcelProperty("是否无限制0-否 1是")
    private Boolean whetherLimitless;

    @ExcelProperty("每人最大领取个数")
    private Integer maxFetch;

    @ExcelProperty("是否开启过期提醒0-不开启 1-开启")
    private Boolean whetherExpireNotice;

    @ExcelProperty("过期前N天提醒")
    private Integer expireNoticeFixedTerm;

    @ExcelProperty("优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用")
    private Boolean whetherForbidPreference;

    @ExcelProperty("是否显示")
    private Integer whetherShow;

    @ExcelProperty("订单的优惠总金额")
    private BigDecimal discountOrderMoney;

    @ExcelProperty("用券总成交额")
    private BigDecimal orderMoney;

    @ExcelProperty("是否禁止发放0-否 1-是")
    private Boolean whetherForbidden;

    @ExcelProperty("使用优惠券购买的商品数量")
    private Integer orderGoodsNum;

    @ExcelProperty("状态（1进行中2已结束-1已关闭）")
    private Integer status;

    @ExcelProperty("有效日期结束时间")
    private Date endTime;

    @ExcelProperty("创建时间")
    private Date createTime;

}
