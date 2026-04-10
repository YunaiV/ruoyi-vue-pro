package cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 质检方案-检测指标项新增/修改 Request VO")
@Data
public class MesQcTemplateIndicatorSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "质检方案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检方案ID不能为空")
    private Long templateId;

    @Schema(description = "质检指标ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检指标不能为空")
    private Long indicatorId;

    @Schema(description = "检测方法", example = "目视检查外观")
    private String checkMethod;

    @Schema(description = "标准值", example = "10.0000")
    private BigDecimal standardValue;

    @Schema(description = "计量单位ID", example = "1")
    private Long unitMeasureId;

    @Schema(description = "误差上限", example = "10.5000")
    private BigDecimal thresholdMax;

    @Schema(description = "误差下限", example = "9.5000")
    private BigDecimal thresholdMin;

    @Schema(description = "说明图URL", example = "https://example.com/doc.png")
    private String docUrl;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
