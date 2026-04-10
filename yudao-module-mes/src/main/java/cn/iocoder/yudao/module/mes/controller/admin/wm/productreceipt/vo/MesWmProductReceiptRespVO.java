package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品收货单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmProductReceiptRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "收货单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PR202603010001")
    @ExcelProperty("收货单编码")
    private String code;

    @Schema(description = "收货单名称", example = "产品收货单-工单001")
    @ExcelProperty("收货单名称")
    private String name;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "生产工单编码", example = "WO202603010001")
    @ExcelProperty("生产工单")
    private String workOrderCode;

    @Schema(description = "产品物料编号", example = "1")
    private Long itemId;

    @Schema(description = "产品物料编码", example = "P001")
    @ExcelProperty("产品物料编码")
    private String itemCode;

    @Schema(description = "产品物料名称", example = "成品A")
    @ExcelProperty("产品物料名称")
    private String itemName;

    @Schema(description = "规格型号", example = "标准型")
    @ExcelProperty("规格型号")
    private String specification;

    @Schema(description = "计量单位名称", example = "件")
    @ExcelProperty("计量单位")
    private String unitMeasureName;

    @Schema(description = "收货日期")
    @ExcelProperty("收货日期")
    private LocalDateTime receiptDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
