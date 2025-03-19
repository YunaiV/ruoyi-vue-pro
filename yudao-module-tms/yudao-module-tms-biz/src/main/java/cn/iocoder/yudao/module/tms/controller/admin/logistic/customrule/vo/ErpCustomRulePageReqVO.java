package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 海关规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpCustomRulePageReqVO extends PageParam {

    @Schema(description = "国家编码")
    private Integer countryCode;

    @Schema(description = "申报品名（英文）")
    private String declaredTypeEn;

    @Schema(description = "申报品名")
    private String declaredType;

    @Schema(description = "申报金额")
    private Double declaredValue;

    @Schema(description = "申报金额币种")
    private Integer declaredValueCurrencyCode;

    @Schema(description = "税率")
    private BigDecimal taxRate;

    @Schema(description = "hs编码")
    private String hscode;

    @Schema(description = "物流属性")
    private Integer logisticAttribute;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

    @Schema(description = "FBA条形码")
    private String fbaBarCode;
//
//    @Schema(description = "SKU（编码）")
//    private String barCode;//ERP产品的SKU编码 //暂时根据productId查询

    @Schema(description = "产品id")
    private Long productId;
}