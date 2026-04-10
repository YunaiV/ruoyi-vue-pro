package cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.item;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 质检方案-产品关联新增/修改 Request VO")
@Data
public class MesQcTemplateItemSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "质检方案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检方案ID不能为空")
    private Long templateId;

    @Schema(description = "产品物料ID（关联 mes_md_item）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品物料ID不能为空")
    private Long itemId;

    @Schema(description = "最低检测数", example = "5")
    private Integer quantityCheck;

    @Schema(description = "最大不合格数", example = "0")
    private Integer quantityUnqualified;

    @Schema(description = "最大致命缺陷率（%）", example = "0")
    private BigDecimal criticalRate;

    @Schema(description = "最大严重缺陷率（%）", example = "0")
    private BigDecimal majorRate;

    @Schema(description = "最大轻微缺陷率（%）", example = "100")
    private BigDecimal minorRate;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
