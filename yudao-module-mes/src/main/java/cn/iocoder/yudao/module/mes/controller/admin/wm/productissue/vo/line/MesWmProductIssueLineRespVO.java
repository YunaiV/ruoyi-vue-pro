package cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 领料出库单行 Response VO")
@Data
public class MesWmProductIssueLineRespVO {

    @Schema(description = "行ID", example = "1")
    private Long id;

    @Schema(description = "领料单ID", example = "1")
    private Long issueId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "领料数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
