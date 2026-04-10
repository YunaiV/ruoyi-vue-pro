package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产退料单行 Response VO")
@Data
public class MesWmReturnIssueLineRespVO {

    @Schema(description = "行 ID", example = "1")
    private Long id;

    @Schema(description = "退料单 ID", example = "1")
    private Long issueId;

    @Schema(description = "物料 ID", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "库存记录 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "退料数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次 ID", example = "1")
    private Long batchId;

    @Schema(description = "批次编码", example = "BAT202601001")
    private String batchCode;

    @Schema(description = "是否需要质检", example = "false")
    private Boolean rqcCheckFlag;

    @Schema(description = "质量状态", example = "0")
    private Integer qualityStatus;

    @Schema(description = "退货检验单 ID", example = "21601")
    private Long rqcId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
