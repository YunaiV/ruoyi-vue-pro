package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 外协发料单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmOutsourceIssueRespVO {

    @Schema(description = "发料单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("发料单ID")
    private Long id;

    @Schema(description = "发料单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WOS202603020001")
    @ExcelProperty("发料单编号")
    private String code;

    @Schema(description = "发料单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "外协发料单001")
    @ExcelProperty("发料单名称")
    private String name;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long vendorId;

    @Schema(description = "供应商编码", example = "V001")
    @ExcelProperty("供应商编码")
    private String vendorCode;

    @Schema(description = "供应商名称", example = "供应商A")
    @ExcelProperty("供应商名称")
    private String vendorName;

    @Schema(description = "生产工单ID", example = "1")
    private Long workOrderId;

    @Schema(description = "生产工单编码", example = "WO202603020001")
    @ExcelProperty("生产工单编码")
    private String workOrderCode;

    @Schema(description = "生产工单名称", example = "生产工单001")
    @ExcelProperty("生产工单名称")
    private String workOrderName;

    @Schema(description = "发料日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("发料日期")
    private LocalDateTime issueDate;

    @Schema(description = "单据状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("单据状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
