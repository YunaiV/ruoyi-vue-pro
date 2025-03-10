package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.resp.ErpPurchaseRequestItemRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseOrderBaseRespVO extends BaseVO {

    @Schema(description = "id")
    private Long id;
    @Schema(description = "采购单编号")
    @ExcelProperty("采购单编号")
    private String no;
    @Schema(description = "单据日期")
    private LocalDateTime noTime;
    @Schema(description = "采购时间")
    @ExcelProperty("采购时间")
    private LocalDateTime orderTime;
    @Schema(description = "订单项列表")
    private List<Item> items;

//    @ExcelProperty("产品名称汇总")
//    private String productNames;

    @Schema(description = "供应商编号")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;
    // ========== 采购入库 ==========
    @Schema(description = "订单采购入库数量")
    private BigDecimal totalInCount;
    // ========== 采购退货（出库）） ==========
    @Schema(description = "订单采购退货数量")
    private BigDecimal totalReturnCount;
    @Schema(description = "交货日期")
    private LocalDateTime deliveryDate;
    // ========== 审核信息 ==========
    @Schema(description = "审核者id")
    @ExcelProperty("审核者id")
    private String auditor;
    private Long auditorId;
    @Schema(description = "审核人名称")
    @ExcelProperty("审核人名称")
    private String auditorName;
    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime auditTime;
    @Schema(description = "审核状态")
    private Integer auditStatus;
    @Schema(description = "开关状态")
    private Integer offStatus;
    @Schema(description = "执行状态")
    private Integer executeStatus;
    @Schema(description = "入库状态")
    private Integer inStatus;
    @Schema(description = "付款状态")
    private Integer payStatus;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "结算账户编号")
    private Long accountId;
    //收获地址
    @Schema(description = "收获地址")
    private String address;
    //付款条款
    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "采购主体编号")
    private Long purchaseEntityId;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;
    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址")
    private String fileUrl;
    @Data
    public static class Item extends BaseVO {
        @Schema(description = "订单项编号")
        private Long id;
        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "erp产品")
        private ErpProductRespVO product;

        @Schema(description = "币别名称")
        private Long currencyName;
        @Schema(description = "产品下单数量")
        private BigDecimal count;
        @Schema(description = "产品单价")
        private BigDecimal productPrice;
        @Schema(description = "税率，百分比")
        private BigDecimal taxPercent;
        @Schema(description = "税额，单位：元")
        private BigDecimal taxPrice;
        @Schema(description = "价税合计")
        private BigDecimal allAmount;
        @Schema(description = "商品行备注")
        private String remark;
        @Schema(description = "供应商付款条款")
        private String supplierRule;
        // ========== 采购入库 ==========
        @Schema(description = "采购入库数量")
        private BigDecimal inCount;
        // ========== 采购退货（入库）） ==========
        @Schema(description = "采购退货数量")
        private BigDecimal returnCount;
        // ========== 关联字段 ==========
//        @Schema(description = "产品名称")
//        private String productName;
//        @Schema(description = "产品单位名称")
//        private String productUnitName;
//        @Schema(description = "产品条码")
//        private String productBarCode;
        // ========== 产品库存相关 ==========
        @Schema(description = "产品存放仓库编号")
        private Long warehouseId; // 该字段仅仅在“详情”和“编辑”时使用
        @Schema(description = "产品存放仓库名称")
        private String warehouseName; // 该字段仅仅在“详情”和“编辑”时使用
        @Schema(description = "报关品名-产品(产品的品牌)")
        private String customsDeclaration;
        @Schema(description = "源单行号")
        private int srcSeq;
        //        private String srcBillTypeId;// 源单类型ID
        @Schema(description = "源单类型")
        private String srcBillTypeName;
        @Schema(description = "源单单号")
        private int srcNo;
        @Schema(description = "x码")
        private String xCode;
        @Schema(description = "箱率")
        private String containerRate;//箱率
        @Schema(description = "型号规格型号")
        private String model;
        // ========== 带出 ==========
        @Schema(description = "可用库存")
        private BigDecimal availableStock;
        @Schema(description = "已入库数量")
        private BigDecimal inCounted;
        /**
         * 关闭状态
         */
        @Schema(description = "关闭状态")
        private Integer offStatus;
        /**
         * 执行状态
         */
        @Schema(description = "执行状态")
        private Integer executeStatus;
        /**
         * 入库状态
         */
        @Schema(description = "入库状态")
        private Integer inStatus;
        /**
         * 付款状态
         */
        @Schema(description = "付款状态")
        private Integer payStatus;
        /**
         * 采购申请项ID
         * {@link ErpPurchaseRequestItemsDO#getId()}
         */
        @Schema(description = "采购申请项ID")
        private Long purchaseApplyItemId;
        @Schema(description = "采购申请单No")
        private String erpPurchaseRequestItemNo;
        @Schema(description = "申请项")
        private ErpPurchaseRequestItemRespVO purchaseRequestItem;

        @Schema(description = "币别id(财务管理-币别维护)")
        private Long currencyId;
    }
}