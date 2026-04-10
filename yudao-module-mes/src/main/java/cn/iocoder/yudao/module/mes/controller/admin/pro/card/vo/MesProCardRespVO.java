package cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产流转卡 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesProCardRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "流转卡编码", example = "CARD-001")
    @ExcelProperty("流转卡编码")
    private String code;

    @Schema(description = "生产工单编号", example = "100")
    private Long workOrderId;

    @Schema(description = "工单编码", example = "WO-001")
    @ExcelProperty("工单编码")
    private String workOrderCode;

    @Schema(description = "工单名称", example = "生产工单-A")
    @ExcelProperty("工单名称")
    private String workOrderName;

    @Schema(description = "批次号", example = "BATCH-001")
    @ExcelProperty("批次号")
    private String batchCode;

    @Schema(description = "产品物料编号", example = "200")
    private Long itemId;

    @Schema(description = "产品编码", example = "P-001")
    @ExcelProperty("产品编码")
    private String itemCode;

    @Schema(description = "产品名称", example = "电路板 A")
    @ExcelProperty("产品名称")
    private String itemName;

    @Schema(description = "规格型号", example = "100x200mm")
    @ExcelProperty("规格型号")
    private String specification;

    @Schema(description = "单位编号", example = "300")
    private Long unitMeasureId;

    @Schema(description = "单位名称", example = "个")
    @ExcelProperty("单位")
    private String unitMeasureName;

    @Schema(description = "流转数量", example = "100.00")
    @ExcelProperty("流转数量")
    private BigDecimal transferedQuantity;

    @Schema(description = "状态", example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
