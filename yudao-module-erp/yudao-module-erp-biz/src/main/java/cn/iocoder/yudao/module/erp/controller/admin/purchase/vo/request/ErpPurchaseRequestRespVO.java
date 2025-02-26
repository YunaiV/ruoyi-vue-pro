package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.module.erp.enums.ErpDictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseRequestRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "32561")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "单据编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据编号")
    private String no;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("申请人")
    private String applicant;

    @Schema(description = "申请人名称", example = "芋道")
    private String applicantName;

    @Schema(description = "申请部门")
    @ExcelProperty("申请部门")
    private String applicationDept;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据日期")
    private LocalDateTime requestTime;

    @Schema(description = "审核状态(0:待审核，1:审核通过，2:审核未通过)", example = "2")
    @ExcelProperty("审核状态(0:待审核，1:审核通过，2:审核未通过)")
    @DictFormat(ErpDictTypeConstants.PURCHASE_REQUEST_APPLICATION_STATUS)
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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "采购订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "产品信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "产品总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("产品总数")
    private Integer totalCount;

    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "产品数量不能为空")
        private Integer count;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;
        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;
        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;
    }
}