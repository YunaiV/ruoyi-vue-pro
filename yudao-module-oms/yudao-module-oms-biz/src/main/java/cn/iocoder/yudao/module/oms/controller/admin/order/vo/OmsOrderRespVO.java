package cn.iocoder.yudao.module.oms.controller.admin.order.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - OMS订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16261")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "所属平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("所属平台")
    private String platformCode;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单号")
    private String no;

    @Schema(description = "外部来源号，即平台订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部来源号，即平台订单号")
    private String sourceNo;

    @Schema(description = "店铺id", example = "8058")
    @ExcelProperty("店铺id")
    private String shopId;

    @Schema(description = "运费")
    @ExcelProperty("运费")
    private BigDecimal shippingCost;

    @Schema(description = "总金额", example = "10077")
    @ExcelProperty("总金额")
    private BigDecimal totalPrice;

    @Schema(description = "买家姓名", example = "李四")
    @ExcelProperty("买家姓名")
    private String buyerName;

    @Schema(description = "邮箱")
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "最迟送达时间")
    @ExcelProperty("最迟送达时间")
    private LocalDateTime deliveryLatestTime;

    @Schema(description = "订单创建时间")
    @ExcelProperty("订单创建时间")
    private LocalDateTime orderCreateTime;

    @Schema(description = "付款时间")
    @ExcelProperty("付款时间")
    private LocalDateTime paymentTime;

    @Schema(description = "电话")
    @ExcelProperty("电话")
    private String telephone;

    @Schema(description = "公司名", example = "张三")
    @ExcelProperty("公司名")
    private String companyName;

    @Schema(description = "收件人国家")
    @ExcelProperty("收件人国家")
    private String buyerCountryCode;

    @Schema(description = "收件人省【或为州】")
    @ExcelProperty("收件人省【或为州】")
    private String state;

    @Schema(description = "城市")
    @ExcelProperty("城市")
    private String city;

    @Schema(description = "区/县")
    @ExcelProperty("区/县")
    private String district;

    @Schema(description = "外部来源原地址，用作备份")
    @ExcelProperty("外部来源原地址，用作备份")
    private String sourceAddress;

    @Schema(description = "地址")
    @ExcelProperty("地址")
    private String address;

    @Schema(description = "门牌号")
    @ExcelProperty("门牌号")
    private String houseNumber;

    @Schema(description = "邮编")
    @ExcelProperty("邮编")
    private String postalCode;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}