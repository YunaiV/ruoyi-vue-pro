package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.base.SrmPurchaseBaseRespVO;
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
public class SrmPurchaseInBaseRespVO extends SrmPurchaseBaseRespVO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "入库单编号")
    @ExcelProperty("入库单编号")
    private String no;

    @Schema(description = "单据日期")
    private LocalDateTime noTime;

    @Schema(description = "收获地址")
    private String address;

    @Schema(description = "付款状态")
    private Integer payStatus;

    @Schema(description = "对账状态(false:未对账 ，true:已对账)")
    private Boolean reconciliationStatus;

    @Schema(description = "入库时间")
    @ExcelProperty("入库时间")
    private LocalDateTime inTime;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "版本号")
    private Long version;

    @Schema(description = "入库项列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "入库项id")
        private Long id;

        @Schema(description = "采购订单项id")
        private Long orderItemId;

        @Schema(description = "仓库id")
        private Long warehouseId;

        @Schema(description = "产品存放仓库名称")
        private String warehouseName;

        @Schema(description = "产品id")
        private Long productId;

        @Schema(description = "型号规格(产品带出)")
        private String model;

        @Schema(description = "产品信息")
        private ErpProductDTO product;

        @Schema(description = "产品单位")
        private Long productUnitId;

        @Schema(description = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "产品数量")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal qty;

        @Schema(description = "税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元")
        private BigDecimal taxPrice;

        @Schema(description = "含税单价")
        private BigDecimal actTaxPrice;

        @Schema(description = "价税合计")
        private BigDecimal allAmount;

        @Schema(description = "备注")
        private String remark;

        //        @Schema(description = "汇率,财务管理-币别维护")
        //        private BigDecimal exchangeRate;

        @Schema(description = "结算日期")
        private LocalDateTime settlementDate;

        // ========== 关联字段 ==========

        @Schema(description = "库存数量")
        private BigDecimal stockCount; // 该字段仅仅在“详情”和“编辑”时使用

        @Schema(description = "箱率")
        private String containerRate;
        // ========== 产品中带出 ==========
        /**
         * 商品体积，单位：m^3 平米
         */
        @Schema(description = "商品总体积,单位：m^3 平米")
        private Double totalVolume;
        /**
         * 商品重量，单位：kg 千克
         */
        @Schema(description = "商品总重量,单位：kg 千克")
        private Double totalWeight;

        //        @Schema(description = "源单行号")
        //        private int srcSeq;
        // 源单类型ID
        //        private String srcBillTypeId;

        //        @Schema(description = "源单类型")
        //        private String srcBillTypeName;

        //        @Schema(description = "源单单号")
        //        private int srcNo;

        @Schema(description = "总价，单位：元,totalPrice = productPrice * count")
        private BigDecimal totalPrice;

        @Schema(description = "币别id(财务管理-币别维护)")
        private Long currencyId;

        @Schema(description = "付款状态")
        private Integer payStatus;

        @Schema(description = "申请人id")
        private Long applicantId;
        //申请人名称
        @Schema(description = "申请人名称")
        private String applicantName;

        @Schema(description = "申请部门id")
        private Long applicationDeptId;
        //申请部门名称
        @Schema(description = "申请部门名称")
        private String applicationDeptName;

        @Schema(description = "来源单据类型")
        private String source;

        @Schema(description = "采购订单编号-展示用(源单单号,采购单)")
        private String orderNo;

        @Schema(description = "报关品名")
        private String declaredType;

        @Schema(description = "条码")
        private String barCode;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品单位名称")
        private String productUnitName;

        @Schema(description = "版本号")
        private Long version;
    }
}