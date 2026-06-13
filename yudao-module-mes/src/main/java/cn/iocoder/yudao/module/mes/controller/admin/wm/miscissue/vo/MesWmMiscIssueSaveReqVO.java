package cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 杂项出库单新增/修改 Request VO")
@Data
public class MesWmMiscIssueSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MI20260302001")
    @NotBlank(message = "出库单编号不能为空")
    private String code;

    @Schema(description = "出库单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存调整出库")
    @NotBlank(message = "出库单名称不能为空")
    private String name;

    @Schema(description = "杂项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "杂项类型不能为空")
    private Integer type;

    @Schema(description = "来源单据类型", example = "PURCHASE_ORDER")
    private String sourceDocType;
    @Schema(description = "来源单据ID", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编号", example = "DOC20260302001")
    private String sourceDocCode;

    @Schema(description = "出库日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出库日期不能为空")
    private LocalDateTime issueDate;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
