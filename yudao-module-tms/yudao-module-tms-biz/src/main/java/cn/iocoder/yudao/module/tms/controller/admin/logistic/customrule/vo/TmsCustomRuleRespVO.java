package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo;


import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP 海关规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsCustomRuleRespVO implements VO {

    @Schema(description = "海关规则id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("海关规则id")
    private Long id;

    @Schema(description = "国家编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家编码")
    private Integer countryCode;

    @Schema(description = "产品id")
    @ExcelProperty("产品id")
    private Long productId;
    /**
     * 产品实体类
     */
    private ErpProductDTO product;

    @Schema(description = "申报品名（英文）")
    @ExcelProperty("申报品名（英文）")
    private String declaredTypeEn;

    @Schema(description = "申报品名")
    @ExcelProperty("申报品名")
    private String declaredType;

    @Schema(description = "申报金额")
    @ExcelProperty("申报金额")
    private Double declaredValue;

    @Schema(description = "申报金额币种")
    @ExcelProperty("申报金额币种")
    private Integer declaredValueCurrencyCode;

    @Schema(description = "税率")
    @ExcelProperty("税率")
    private BigDecimal taxRate;

    @Schema(description = "hs编码")
    @ExcelProperty("hs编码")
    private String hscode;

    @Schema(description = "物流属性")
    @ExcelProperty("物流属性")
    private Integer logisticAttribute;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "条形码")
    @ExcelProperty("条形码")
    private String fbaBarCode;
}