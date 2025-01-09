package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.base.ErpPurchaseBaseRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseInBaseRespVO extends ErpPurchaseBaseRespVO {


    @Schema(description = "入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    @ExcelProperty("入库单编号")
    private String no;

    @Schema(description = "入库状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("入库状态")
    private Integer status;

    @Schema(description = "入库时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("入库时间")
    private LocalDateTime inTime;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    private Long orderId;
    @Schema(description = "采购订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    private String orderNo;

    @Schema(description = "入库项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "对账状态(false:未对账 ，true:已对账)", example = "false")
    private Boolean reconciliationStatus;

    @Data
    public static class Item {

        @Schema(description = "入库项编号", example = "11756")
        private Long id;

        @Schema(description = "采购订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private Long orderItemId;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long warehouseId;

        @Schema(description = "产品存放仓库名称")
        private String warehouseName;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "含税单价", example = "120.00")
        private double actTaxPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @Schema(description = "汇率,财务管理-币别维护", example = "5.8")
        private BigDecimal exchangeRate;

        @Schema(description = "结算日期", example = "2025-1-1")
        private LocalDateTime settlementDate;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;
        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;
        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal stockCount; // 该字段仅仅在“详情”和“编辑”时使用

        @Schema(description = "箱率")
        private String containerRate;
        // ========== 产品中带出 ==========
        /**
         * 商品体积，单位：m^3 平米
         */
        @Schema(description = "商品总体积,单位：m^3 平米", example = "2.5")
        private Double totalVolume;
        /**
         * 商品重量，单位：kg 千克
         */
        @Schema(description = "商品总重量,单位：kg 千克", example = "1.2")
        private Double totalWeight;

        @Schema(description = "型号规格型号")
        private String model;

        @Schema(description = "报关品名-产品(产品的品牌)")
        private String customsDeclaration;

        @Schema(description = "源单行号")
        private int srcSeq;
        // 源单类型ID
//        private String srcBillTypeId;

        @Schema(description = "源单类型")
        private String srcBillTypeName;

        @Schema(description = "源单单号")
        private int srcNo;

        @Schema(description = "价税合计")
        private double allAmount;
    }

}