package cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 待检任务 Response VO")
@Data
public class MesQcPendingInspectRespVO {

    @Schema(description = "来源单据 ID", example = "100")
    private Long sourceDocId;

    @Schema(description = "来源单据类型（MesBizTypeConstants）", example = "100")
    private Integer sourceDocType;

    @Schema(description = "来源单据编号", example = "AN2025001")
    private String sourceDocCode;

    @Schema(description = "来源单据行 ID", example = "200")
    private Long sourceLineId;

    @Schema(description = "检验类型（MesQcTypeEnum）", example = "1")
    private Integer qcType;

    // ========== 物料 ==========

    @Schema(description = "物料 ID", example = "10")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "物料名称", example = "物料A")
    private String itemName;

    @Schema(description = "规格型号", example = "100mm*50mm")
    private String specification;

    @Schema(description = "单位名称", example = "个")
    private String unitName;

    // ========== 数量 ==========

    @Schema(description = "待检数量", example = "100.00")
    private BigDecimal quantity;

    // ========== 供应商（IQC 场景） ==========

    @Schema(description = "供应商 ID", example = "5")
    private Long vendorId;

    @Schema(description = "供应商名称", example = "供应商A")
    private String vendorName;

    // ========== 工单/工作站/任务（IPQC/RQC 场景） ==========

    @Schema(description = "生产工单 ID", example = "10")
    private Long workOrderId;

    @Schema(description = "工作站 ID", example = "20")
    private Long workstationId;

    @Schema(description = "工作站名称", example = "工作站A")
    private String workstationName;

    @Schema(description = "生产任务 ID", example = "30")
    private Long taskId;

    @Schema(description = "生产任务编码", example = "TASK2025001")
    private String taskCode;

    // ========== 客户（OQC/RQC 场景） ==========

    @Schema(description = "客户 ID", example = "40")
    private Long clientId;

    @Schema(description = "客户名称", example = "客户A")
    private String clientName;

    // ========== 时间 ==========

    @Schema(description = "记录时间")
    private LocalDateTime recordTime;

}