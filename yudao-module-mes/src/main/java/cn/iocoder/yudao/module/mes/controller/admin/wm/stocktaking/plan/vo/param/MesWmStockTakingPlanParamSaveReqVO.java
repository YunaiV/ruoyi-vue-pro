package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 盘点方案参数新增/修改 Request VO")
@Data
public class MesWmStockTakingPlanParamSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "方案 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "方案 ID 不能为空")
    private Long planId;

    @Schema(description = "参数类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    @NotNull(message = "参数类型不能为空")
    private Integer type;

    @Schema(description = "参数值 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "参数值 ID 不能为空")
    private Long valueId;

    @Schema(description = "参数值编码", example = "WH001")
    private String valueCode;

    @Schema(description = "参数值名称", example = "原料仓")
    private String valueName;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
