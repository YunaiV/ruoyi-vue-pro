package cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 现金流量辅助数据 Response VO")
@Data
public class FmsCashFlowAdjustmentRespVO {

    @Schema(description = "数据编号", example = "1024")
    private Long id;

    @Schema(description = "项目名称", example = "支付给职工的工资")
    private String name;

    @Schema(description = "行次", example = "1")
    private Integer rowNo;

    @Schema(description = "公式")
    private String formula;

    @Schema(description = "说明")
    private String remark;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;

    @Schema(description = "本期金额", example = "100.00")
    private BigDecimal currentAmount;

    @Schema(description = "本年累计金额", example = "200.00")
    private BigDecimal yearAmount;

    @Schema(description = "层级", example = "1")
    private Integer level;

}
