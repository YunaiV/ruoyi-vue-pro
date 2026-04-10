package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 到货通知单行新增/修改 Request VO")
@Data
public class MesWmArrivalNoticeLineSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "到货通知单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货通知单编号不能为空")
    private Long noticeId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "500.00")
    @NotNull(message = "到货数量不能为空")
    @DecimalMin(value = "0.01", message = "到货数量必须大于 0")
    private BigDecimal arrivalQuantity;

    @Schema(description = "是否需要来料检验", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否需要来料检验不能为空")
    private Boolean iqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
