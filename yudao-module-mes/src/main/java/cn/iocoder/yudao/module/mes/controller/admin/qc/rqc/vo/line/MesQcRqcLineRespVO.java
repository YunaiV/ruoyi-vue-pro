package cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 退货检验行 Response VO")
@Data
@Accessors(chain = true)
public class MesQcRqcLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "退货检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long rqcId;

    @Schema(description = "检测指标 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long indicatorId;

    @Schema(description = "检测指标编码", example = "IND001")
    private String indicatorCode;

    @Schema(description = "检测指标名称", example = "外观")
    private String indicatorName;

    @Schema(description = "检测指标类型", example = "1")
    private Integer indicatorType;

    @Schema(description = "检测工具", example = "卡尺")
    private String tool;

    @Schema(description = "检测方法", example = "目测")
    private String checkMethod;

    @Schema(description = "标准值", example = "10.5000")
    private BigDecimal standardValue;

    @Schema(description = "计量单位 ID", example = "3")
    private Long unitMeasureId;

    @Schema(description = "计量单位名称", example = "毫米")
    private String unitMeasureName;

    @Schema(description = "误差上限", example = "11.0000")
    private BigDecimal maxThreshold;

    @Schema(description = "误差下限", example = "10.0000")
    private BigDecimal minThreshold;

    @Schema(description = "致命缺陷数量", example = "0")
    private Integer criticalQuantity;

    @Schema(description = "严重缺陷数量", example = "0")
    private Integer majorQuantity;

    @Schema(description = "轻微缺陷数量", example = "1")
    private Integer minorQuantity;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
