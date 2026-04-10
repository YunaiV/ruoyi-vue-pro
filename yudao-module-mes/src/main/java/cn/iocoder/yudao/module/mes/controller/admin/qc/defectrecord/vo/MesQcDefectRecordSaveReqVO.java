package cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 质检缺陷记录新增/修改 Request VO")
@Data
public class MesQcDefectRecordSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验类型不能为空")
    @InEnum(MesQcTypeEnum.class)
    private Integer qcType;

    @Schema(description = "检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "检验单 ID 不能为空")
    private Long qcId;

    @Schema(description = "检验行 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "检验行 ID 不能为空")
    private Long lineId;

    @Schema(description = "缺陷描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "表面划伤")
    @NotEmpty(message = "缺陷描述不能为空")
    private String name;

    @Schema(description = "缺陷等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺陷等级不能为空")
    @InEnum(MesQcDefectLevelEnum.class)
    private Integer level;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺陷数量不能为空")
    private Integer quantity;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
