package cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 杂项出库单 Response VO")
@Data
public class MesWmMiscIssueRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MI20260302001")
    private String code;

    @Schema(description = "出库单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存调整出库")
    private String name;

    @Schema(description = "杂项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "来源单据ID", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编号", example = "DOC20260302001")
    private String sourceDocCode;

    @Schema(description = "来源单据类型", example = "PURCHASE_ORDER")
    private String sourceDocType;

    @Schema(description = "出库日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime issueDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
