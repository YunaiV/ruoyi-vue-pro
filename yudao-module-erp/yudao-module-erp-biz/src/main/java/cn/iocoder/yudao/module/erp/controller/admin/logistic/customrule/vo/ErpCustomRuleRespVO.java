package cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP 海关规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpCustomRuleRespVO implements VO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "国家编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家编码")
    private Integer countryCode;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "报关")
    @ExcelProperty("类型")
    private String type;

    @Schema(description = "供应商产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("供应商产品编号")
    @Trans(type = TransType.SIMPLE, target = ErpSupplierProductDO.class,fields = "code",ref = "supplierProductCode")
    private Long supplierProductId;

    @Schema(description = "供应商产品编号")
    @ExcelProperty("供应商产品编号")
    private String supplierProductCode;

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

}