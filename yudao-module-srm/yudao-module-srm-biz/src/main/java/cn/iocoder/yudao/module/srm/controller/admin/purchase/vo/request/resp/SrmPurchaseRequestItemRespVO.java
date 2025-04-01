package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.resp;

import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SrmPurchaseRequestItemRespVO extends BaseVO {

    // ========== 基本信息 ==========

    @Schema(description = "订单项编号")
    @ExcelProperty("订单项编号")
    private Long id;

    @Schema(description = "产品编码", example = "FTC1607AWB")
    @ExcelProperty("产品编码")
    private String no;

    // ========== 产品信息 ==========
    //
    //    @Schema(description = "产品信息")
    //    private ErpProductDTO product;

    @Schema(description = "产品编号")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "产品报关品名")
    private String declaredType;

    @Schema(description = "报关品名英文")
    private String declaredTypeEn;

    @Schema(description = "产品sku")
    private String barCode;

    @Schema(description = "产品名称", example = "巧克力")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品sku", example = "A9985")
    @ExcelProperty("产品条码")
    private String productBarCode;

    @Schema(description = "产品单位名称", example = "盒")
    @ExcelProperty("产品单位名称")
    private String productUnitName;

    @Schema(description = "产品单位ID")
    @ExcelProperty("产品单位ID")
    private Long productUnitId;

    @Schema(description = "仓库id")
    private Long warehouseId;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    // ========== 数量与价格 ==========

    @Schema(description = "产品数量", example = "100")
    @NotNull(message = "产品数量不能为空")
    @ExcelProperty("产品数量")
    private Integer qty;

    @Schema(description = "参考单价")
    @ExcelProperty("参考单价")
    private BigDecimal referenceUnitPrice;

    @Schema(description = "含税单价", example = "100.00")
    @ExcelProperty("含税单价")
    private BigDecimal actTaxPrice;

    @Schema(description = "价税合计")
    @ExcelProperty("价税合计")
    private BigDecimal allAmount;

    @Schema(description = "税额，单位：元")
    @ExcelProperty("税额")
    private BigDecimal taxPrice;

    @Schema(description = "税率，百分比")
    @ExcelProperty("税率")
    private BigDecimal taxPercent;

    @Schema(description = "批准数量")
    @ExcelProperty("批准数量")
    private Integer approvedQty;

    // ========== 其他状态信息 ==========
    @Schema(description = "关闭状态（已关闭，已开启）")
    @ExcelProperty("关闭状态")
    private Integer offStatus;

    @Schema(description = "未订购数量", example = "100")
    @ExcelProperty("未订购数量")
    private Integer unOrderCount;

    @Schema(description = "已订购数量")
    private Integer orderClosedQty;

    @Schema(description = "已入库数量", example = "100")
    @ExcelProperty("已入库数量")
    private Integer inboundClosedQty;

    @Schema(description = "行采购状态")
    @ExcelProperty("行采购状态")
    private Integer orderStatus;

    @Schema(description = "供应商id")
    @ExcelProperty("供应商id")
    private Long supplierId;

    @Schema(description = "供应商名称")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "收货地址")
    @ExcelProperty("收货地址")
    private String delivery;
    //期望到货日期
    @Schema(description = "期望到货日期")
    private LocalDateTime expectArrivalDate;

    @Schema(description = "版本号")
    private Long version;
}
