package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货单行新增/修改 Request VO")
@Data
public class MesWmReturnSalesLineSaveReqVO {

    @Schema(description = "行ID", example = "1")
    private Long id;

    @Schema(description = "退货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退货单ID不能为空")
    private Long returnId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料ID不能为空")
    private Long itemId;

    @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "退货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "退货数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "B20250101")
    private String batchCode;

    @Schema(description = "是否需要质检", example = "true")
    private Boolean rqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
