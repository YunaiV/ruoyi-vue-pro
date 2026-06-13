package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 杂项入库单新增/修改 Request VO")
@Data
public class MesWmMiscReceiptSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "入库单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "MR2026030001")
    @NotEmpty(message = "入库单编码不能为空")
    private String code;

    @Schema(description = "入库单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "退料入库单")
    @NotEmpty(message = "入库单名称不能为空")
    private String name;

    @Schema(description = "杂项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "杂项类型不能为空")
    private Integer type;

    @Schema(description = "来源单据类型", example = "WORK_ORDER")
    private String sourceDocType;
    @Schema(description = "来源单据 ID", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编码", example = "WO2026030001")
    private String sourceDocCode;

    @Schema(description = "入库日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入库日期不能为空")
    private LocalDateTime receiptDate;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
