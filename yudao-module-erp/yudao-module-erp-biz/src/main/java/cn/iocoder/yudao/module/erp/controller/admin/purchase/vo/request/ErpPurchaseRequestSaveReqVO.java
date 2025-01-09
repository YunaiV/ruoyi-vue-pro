package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class ErpPurchaseRequestSaveReqVO {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "32561")
    private Long id;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请人不能为空")
    private String applicant;

    @Schema(description = "申请部门")
    private String applicationDept;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    private LocalDateTime requestTime;

    @Schema(description = "商品信息")
    @NotNull(message = "商品信息不能为空")
    @NotEmpty(message = "商品信息不能为空")
    private List<@Valid ErpPurchaseRequestItemsSaveReqVO> items;

    @Schema(description = "币别id")
    private Long currencyId;

    @Schema(description = "单据编号", example = "CGDD-20250108-000027")
    private String no;

    @Schema(description = "单据标签")
    private String tag;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "收货地址")
    private String receviceDelivery;

    @Data
    public static class Item {

        @Schema(description = "订单项编号")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        private BigDecimal productPrice;

        @Schema(description = "含税单价", example = "100.00")
        @DecimalMin(value = "0.00", message = "含税单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        @DecimalMin(value = "1", message = "产品数量必须大于0")
        private BigDecimal count;

        @Schema(description = "增值税税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @Schema(description = "供应商产品编号", example = "1007")
        @Pattern(regexp = "\\d+$", message = "供应商产品编号格式不正确")
        @NotNull(message = "供应商产品编号不能为空")
        private String supplierProductId;

        @Schema(description = "产品名称")
        private String ItemName;
        // ========== 采购入库 ==========
        /**
         * 采购入库数量
         */
        @DecimalMin(value = "0.00", message = "采购入库数量不能小于0")
        private BigDecimal inCount;

        @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
        private BigDecimal discountPercent;
        // ========== 仓库相关 ==========
        @Schema(description = "仓库编号", example = "3")
        private String warehouseId; // 仓库编号

        @Schema(description = "参考单价")
        private BigDecimal ReferenceUnitPrice;
    }
}