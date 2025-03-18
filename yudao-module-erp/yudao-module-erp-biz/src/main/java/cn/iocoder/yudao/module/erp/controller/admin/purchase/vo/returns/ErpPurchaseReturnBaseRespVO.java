package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.base.ErpPurchaseBaseRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购退货 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseReturnBaseRespVO extends ErpPurchaseBaseRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "退货单编号")
    @ExcelProperty("退货单编号")
    private String no;

    @Schema(description = "退货状态")
    @ExcelProperty("退货状态")
    private Integer status;

    @Schema(description = "退货时间")
    @ExcelProperty("退货时间")
    private LocalDateTime returnTime;

    @Schema(description = "产品信息")
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "供应商id")
    private Long supplierId;
    @Schema(description = "结算供应商名称")
    private Long settlementSupplierName;
    @Schema(description = "结算账户id")
    private Long accountId;
    @Schema(description = "结算账户名称")
    private Long settlementAccountName;

    //合计
    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     * <p>
     * totalPrice = totalProductPrice + totalTaxPrice - discountPrice + otherPrice
     */
    private BigDecimal totalPrice;
    /**
     * 已退款金额，单位：元
     * <p>
     * 目的：和 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO} 结合，记录已支付金额
     */
    private BigDecimal refundPrice;

    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     * <p>
     * discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 其它金额，单位：元
     */
    private BigDecimal otherPrice;
    /**
     * 币种编号
     */
    private Long currencyId;
    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;

    @Schema(description = "退货项列表")
    private List<Item> items;
    @Data
    public static class Item {

        @Schema(description = "退货项编号", example = "11756")
        private Long id;

        @Schema(description = "采购订单项编号")
        private Long orderItemId;

        /**
         * 采购订单编号
         * <p>
         * 关联 {@link ErpPurchaseOrderDO#getId()}
         */
        private Long orderId;
        /**
         * 采购订单号
         * <p>
         * 冗余 {@link ErpPurchaseOrderDO#getNo()}
         */
        private String orderNo;

        @Schema(description = "仓库编号")
        private Long warehouseId;

        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "产品单位单位")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        /**
         * 含税单价
         */
        private BigDecimal actTaxPrice;

        @Schema(description = "产品数量")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        /**
         * 总价，单位：元
         * <p>
         * totalPrice = productPrice * count
         */
        private BigDecimal totalPrice;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称")
        private String productName;
        @Schema(description = "产品条码")
        private String productBarCode;
        @Schema(description = "产品单位名称")
        private String productUnitName;

        @Schema(description = "库存数量")
        private BigDecimal stockCount; // 该字段仅仅在“详情”和“编辑”时使用

        @Schema(description = "价税合计")
        private  BigDecimal allAmount;

        @Schema(description = "报关品名-产品(产品的品牌)")
        private String customsDeclaration;

//        @Schema(description = "源单行号")
//        private int srcSeq;
//        // 源单类型ID
        /// /        private String srcBillTypeId;
//
//        @Schema(description = "源单类型")
//        private String srcBillTypeName;
//
//        @Schema(description = "源单单号")
//        private int srcNo;

        @Schema(description = "箱率")
        private String containerRate;
    }

}