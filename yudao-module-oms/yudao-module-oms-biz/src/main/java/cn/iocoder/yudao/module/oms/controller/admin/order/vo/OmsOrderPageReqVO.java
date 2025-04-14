package cn.iocoder.yudao.module.oms.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OMS订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class OmsOrderPageReqVO extends PageParam {

    @Schema(description = "所属平台")
    private String platformCode;

    @Schema(description = "订单号")
    private String no;

    @Schema(description = "外部来源号，即平台订单号")
    private String sourceNo;

    @Schema(description = "店铺id", example = "8058")
    private String shopId;

    @Schema(description = "运费")
    private BigDecimal shippingCost;

    @Schema(description = "总金额", example = "10077")
    private BigDecimal totalPrice;

    @Schema(description = "买家姓名", example = "李四")
    private String buyerName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "最迟送达时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] deliveryLatestTime;

    @Schema(description = "订单创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderCreateTime;

    @Schema(description = "付款时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] paymentTime;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "公司名", example = "张三")
    private String companyName;

    @Schema(description = "收件人国家")
    private String buyerCountryCode;

    @Schema(description = "收件人省【或为州】")
    private String state;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "外部来源原地址，用作备份")
    private String sourceAddress;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "门牌号")
    private String houseNumber;

    @Schema(description = "邮编")
    private String postalCode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}