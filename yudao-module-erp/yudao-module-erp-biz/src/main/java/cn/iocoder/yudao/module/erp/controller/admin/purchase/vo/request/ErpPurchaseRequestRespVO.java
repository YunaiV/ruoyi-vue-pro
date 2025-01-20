package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.erp.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseRequestRespVO extends BaseVO {

    @Schema(description = "id")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "单据编号")
    @ExcelProperty("单据编号")
    private String no;

    @Schema(description = "申请人")
    @ExcelProperty("申请人")
    private String applicant;

    @Schema(description = "申请部门")
    @ExcelProperty("申请部门")
    private String applicationDept;

    @Schema(description = "单据日期")
    @ExcelProperty("单据日期")
    private LocalDateTime requestTime;


    // ========== 审核信息 ==========


    @Schema(description = "审核者")
    @ExcelProperty("审核者")
    private String auditor;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime auditTime;

    // ========== 订单项列表 ==========
    @Schema(description = "采购订单项列表")
    private List<Item> items;

    @Schema(description = "产品信息")
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "产品总数", example = "100")
    @ExcelProperty("产品总数")
    private Integer totalCount;

    // ========== 申请单创建和更新 ==========
    @Schema(description = "制单时间")
    @ExcelProperty("制单时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间", example = "2025-01-17 10:10:00")
    @ExcelProperty("最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "制单人", example = "admin")
    @ExcelProperty("制单人")
    private String creator;

    @Schema(description = "更新者", example = "admin")
    @ExcelProperty("更新者")
    private String updater;
    // ========== 申请单计算 ==========
    // ========== 状态 ==========
    @Schema(description = "审核状态（待审核，审核通过，审核未通过）")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.AUDIT_STATUS)
    private Integer status;

    @Schema(description = "关闭状态（已关闭，已开启）")
    @ExcelProperty("关闭状态")
    private Integer offStatus;

    @Schema(description = "订购状态（部分订购，全部订购）")
    @ExcelProperty("订购状态")
    private Integer orderStatus;


    @Data
    public static class Item extends BaseVO {

        // ========== 基本信息 ==========

        @Schema(description = "订单项编号")
        @ExcelProperty("订单项编号")
        private Long id;

        @Schema(description = "产品编码", example = "FTC1607AWB")
        @ExcelProperty("产品编码")
        private String no;

        // ========== 产品信息 ==========
        @Schema(description = "产品编号")
        @ExcelProperty("产品编号")
        private Long productId;

        @Schema(description = "产品名称", example = "巧克力")
        @ExcelProperty("产品名称")
        private String productName;

        @Schema(description = "产品sku", example = "A9985")
        @ExcelProperty("产品条码")
        private String productBarCode;

        @Schema(description = "产品单位名称", example = "盒")
        @ExcelProperty("产品单位名称")
        private String productUnitName;

        @Schema(description = "仓库id")
        @ExcelProperty("仓库id")
        private Long warehouseId;

        @Schema(description = "仓库名称")
        @ExcelProperty("仓库名称")
        private String warehouseName;

        // ========== 数量与价格 ==========

        @Schema(description = "产品数量", example = "100")
        @NotNull(message = "产品数量不能为空")
        @ExcelProperty("产品数量")
        private Integer count;

        @Schema(description = "已入库数量")
        @ExcelProperty("已入库数量")
        private String inQty;

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
        private Integer approveCount;
        // ========== 其他状态信息 ==========
        @Schema(description = "关闭状态（已关闭，已开启）")
        @ExcelProperty("关闭状态")
        private String offStatus;

        //未订购数量
        @Schema(description = "未订购数量", example = "100")
        @ExcelProperty("未订购数量")
        private Integer unOrderCount;
        //已订购数量
        @Schema(description = "已订购数量", example = "100")
        @ExcelProperty("已订购数量")
        private Integer orderCount;
        //已入库数量
        @Schema(description = "已入库数量", example = "100")
        @ExcelProperty("已入库数量")
        private Integer inCount;
        //行采购状态
        @Schema(description = "行采购状态")
        @ExcelProperty("行采购状态")
        private String orderStatus;
    }

}
