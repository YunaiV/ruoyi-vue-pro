package cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 缺陷类型新增/修改 Request VO")
@Data
public class MesQcDefectSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF001")
    @NotEmpty(message = "缺陷编码不能为空")
    private String code;

    @Schema(description = "缺陷描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "外观缺陷")
    @NotEmpty(message = "缺陷描述不能为空")
    private String name;

    @Schema(description = "检测项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "检测项类型不能为空")
    private Integer type;

    @Schema(description = "缺陷等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺陷等级不能为空")
    @InEnum(MesQcDefectLevelEnum.class)
    private Integer level;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
