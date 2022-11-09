package cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 优惠券 Excel VO
 *
 * @author wxr
 */
@Data
public class CouponExcelVO {

    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("优惠券类型 reward-满减 discount-折扣 random-随机")
    private String type;

    @ExcelProperty("优惠券名称")
    private String name;

    @ExcelProperty("优惠券类型id")
    private Long couponTypeId;

    @ExcelProperty("优惠券编码")
    private String couponCode;

    @ExcelProperty("领用人")
    private Long memberId;

    @ExcelProperty("优惠券使用订单id")
    private Long useOrderId;

    @ExcelProperty("适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用")
    private Boolean goodsType;

    @ExcelProperty("适用商品id")
    private String goodsIds;

    @ExcelProperty("最小金额")
    private BigDecimal atLeast;

    @ExcelProperty("面额")
    private BigDecimal money;

    @ExcelProperty("1 =< 折扣 <= 9.9 当type为discount时需要添加")
    private BigDecimal discount;

    @ExcelProperty("最多折扣金额 当type为discount时可选择性添加")
    private BigDecimal discountLimit;

    @ExcelProperty("优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用")
    private Boolean whetherForbidPreference;

    @ExcelProperty("是否开启过期提醒0-不开启 1-开启")
    private Boolean whetherExpireNotice;

    @ExcelProperty("过期前N天提醒")
    private Integer expireNoticeFixedTerm;

    @ExcelProperty("是否已提醒")
    private Boolean whetherNoticed;

    @ExcelProperty("优惠券状态 1已领用（未使用） 2已使用 3已过期")
    private Integer state;

    @ExcelProperty("获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取")
    private Boolean getType;

    @ExcelProperty("领取时间")
    private Date fetchTime;

    @ExcelProperty("使用时间")
    private Date useTime;

    @ExcelProperty("可使用的开始时间")
    private Date startTime;

    @ExcelProperty("有效期结束时间")
    private Date endTime;

    @ExcelProperty("创建时间")
    private Date createTime;

}
