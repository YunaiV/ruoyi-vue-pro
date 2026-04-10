package cn.iocoder.yudao.module.mes.controller.admin.wm.itemconsume.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 物料消耗记录行 Response VO")
@Data
public class MesWmItemConsumeLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "消耗记录编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long consumeId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;

    @Schema(description = "消耗数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "批次编号")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ==================== 关联信息 ====================

    @Schema(description = "物资编码")
    private String itemCode;

    @Schema(description = "物资名称")
    private String itemName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "单位")
    private String unitName;

}
