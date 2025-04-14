package cn.iocoder.yudao.module.oms.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - OMS订单新增/修改 Request VO")
@Data
public class OmsOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16261")
    private Long id;

    @Schema(description = "所属平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "所属平台不能为空")
    private String platformCode;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单号不能为空")
    private String no;

    @Schema(description = "外部来源号，即平台订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部来源号，即平台订单号不能为空")
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
    private LocalDateTime deliveryLatestTime;

    @Schema(description = "订单创建时间")
    private LocalDateTime orderCreateTime;

    @Schema(description = "付款时间")
    private LocalDateTime paymentTime;

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

}