package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 销售出库单行新增/修改 Request VO")
@Data
public class MesWmProductSalesLineSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "出库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "出库单ID不能为空")
    private Long salesId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料不能为空")
    private Long itemId;

    @Schema(description = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "出库数量不能为空")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "B20260301001")
    private String batchCode;

    @Schema(description = "是否出厂检验", example = "true")
    private Boolean oqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
