package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.module.erp.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseRequestRespVO {

    @Schema(description = "id")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "单据编号")
    @ExcelProperty("单据编号")
    private String no;

    @Schema(description = "申请人")
    @ExcelProperty("申请人")
    private String applicant;

    @Schema(description = "申请人名称", example = "张三")
    private String applicantName;

    @Schema(description = "申请部门")
    @ExcelProperty("申请部门")
    private String applicationDept;

    @Schema(description = "单据日期")
    @ExcelProperty("单据日期")
    private LocalDateTime requestTime;

    @Schema(description = "审核状态(0:待审核，1:审核通过，2:审核未通过)", example = "2")
    @ExcelProperty("审核状态(0:待审核，1:审核通过，2:审核未通过)")
    @DictFormat(DictTypeConstants.PURCHASE_REQUEST_APPLICATION_STATUS)
    private Integer status;

    @Schema(description = "关闭状态（0已关闭，1已开启）", example = "1")
    @ExcelProperty("关闭状态（0已关闭，1已开启）")
    private Integer offStatus;

    @Schema(description = "订购状态（0部分订购，1全部订购）", example = "1")
    @ExcelProperty("订购状态（0部分订购，1全部订购）")
    private Integer orderStatus;

    @Schema(description = "审核者")
    @ExcelProperty("审核者")
    private String auditor;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "采购订单项列表")
    private List<Item> items;

    @Schema(description = "产品信息")
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "产品总数", example = "100")
    @ExcelProperty("产品总数")
    private Integer totalCount;

    @Schema(description = "币别名称")
    private Long currencyName;

    @Data
    public static class Item {

        @Schema(description = "订单项编号")
        private Long id;

        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "产品编码", example = "FTC1607AWB")
        private String no;

        @Schema(description = "产品数量", example = "100")
        @NotNull(message = "产品数量不能为空")
        private Integer count;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", example = "巧克力")
        private String productName;
        @Schema(description = "产品条码", example = "A9985")
        private String productBarCode;
        @Schema(description = "产品单位名称", example = "盒")
        private String productUnitName;

        private String warehouseName; // 仓库名称

        @Schema(description = "已入库数量")
        private String inQty;

        @Schema(description = "参考单价")
        private BigDecimal ReferenceUnitPrice;

        @Schema(description = "含税单价", example = "100.00")
        @DecimalMin(value = "0.00", message = "含税产品单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "价税合计")
        private BigDecimal allAmount;
    }
}