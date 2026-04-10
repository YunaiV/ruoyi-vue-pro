package cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 批次管理 Response VO")
@Data
public class MesWmBatchRespVO {

    @Schema(description = "批次 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "批次编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATCH20250314001")
    private String code;

    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "物料名称", example = "产品A")
    private String itemName;

    @Schema(description = "规格型号", example = "100*200")
    private String itemSpecification;

    @Schema(description = "单位名称", example = "个")
    private String unitName;

    @Schema(description = "生产日期")
    private LocalDateTime produceDate;

    @Schema(description = "有效期")
    private LocalDateTime expireDate;

    @Schema(description = "入库日期")
    private LocalDateTime receiptDate;

    @Schema(description = "供应商 ID", example = "1")
    private Long vendorId;

    @Schema(description = "供应商编码", example = "V001")
    private String vendorCode;

    @Schema(description = "供应商名称", example = "供应商A")
    private String vendorName;

    @Schema(description = "客户 ID", example = "1")
    private Long clientId;

    @Schema(description = "客户编码", example = "C001")
    private String clientCode;

    @Schema(description = "客户名称", example = "客户A")
    private String clientName;

    @Schema(description = "采购订单编号", example = "PO20250314001")
    private String purchaseOrderCode;

    @Schema(description = "销售订单编号", example = "SO20250314001")
    private String salesOrderCode;

    @Schema(description = "生产工单 ID", example = "1")
    private Long workOrderId;

    @Schema(description = "生产工单编号", example = "WO20250314001")
    private String workOrderCode;

    @Schema(description = "生产任务 ID", example = "1")
    private Long taskId;

    @Schema(description = "生产任务编号", example = "TASK20250314001")
    private String taskCode;

    @Schema(description = "工作站 ID", example = "1")
    private Long workstationId;

    @Schema(description = "工作站编码", example = "WS001")
    private String workstationCode;

    @Schema(description = "工具 ID", example = "1")
    private Long toolId;

    @Schema(description = "工具编号", example = "TOOL001")
    private String toolCode;

    @Schema(description = "模具 ID", example = "1")
    private Long moldId;

    @Schema(description = "生产批号", example = "LOT20250314001")
    private String lotNumber;

    @Schema(description = "质量状态", example = "1")
    private Integer qualityStatus;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
