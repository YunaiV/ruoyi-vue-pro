package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.base.ErpPurchaseBaseRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseOrderBaseRespVO extends ErpPurchaseBaseRespVO {

    @Schema(description = "采购单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    @ExcelProperty("采购单编号")
    private String no;

    @Schema(description = "采购状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("采购状态编号")
    private Integer status;

    @Schema(description = "采购状态描述", example = "审核中")
    @ExcelProperty("采购状态")
    private String statusDesc;

    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("采购时间")
    private LocalDateTime orderTime;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "产品信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品信息")
    private String productNames;

    // ========== 采购入库 ==========

    @Schema(description = "订单采购入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal totalInCount;

    // ========== 采购退货（出库）） ==========

    @Schema(description = "订单采购退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal totalReturnCount;


    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
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

        @Schema(description = "价税合计")
        private double allAmount;

        @Schema(description = "备注", example = "随便")
        private String remark;

        // ========== 采购入库 ==========

        @Schema(description = "采购入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal inCount;

        // ========== 采购退货（入库）） ==========

        @Schema(description = "采购退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal returnCount;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;
        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;
        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;

        // ========== 产品库存相关 ==========
        @Schema(description = "产品存放仓库编号")
        private Long warehouseId; // 该字段仅仅在“详情”和“编辑”时使用
        @Schema(description = "产品存放仓库名称")
        private String warehouseName; // 该字段仅仅在“详情”和“编辑”时使用

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

        @Schema(description = "箱率")
        private String containerRate;//箱率
    }

}