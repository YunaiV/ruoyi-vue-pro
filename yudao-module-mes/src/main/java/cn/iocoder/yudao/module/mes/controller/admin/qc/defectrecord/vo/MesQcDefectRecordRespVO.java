package cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 质检缺陷记录 Response VO")
@Data
public class MesQcDefectRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer qcType;

    @Schema(description = "检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long qcId;

    @Schema(description = "检验行 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Long lineId;

    @Schema(description = "缺陷描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "表面划伤")
    private String name;

    @Schema(description = "缺陷等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "缺陷数量", example = "1")
    private Integer quantity;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
