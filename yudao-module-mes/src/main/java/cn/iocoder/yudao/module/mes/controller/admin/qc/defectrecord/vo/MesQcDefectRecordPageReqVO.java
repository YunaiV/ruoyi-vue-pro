package cn.iocoder.yudao.module.mes.controller.admin.qc.defectrecord.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 质检缺陷记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcDefectRecordPageReqVO extends PageParam {

    @Schema(description = "检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验类型不能为空")
    @InEnum(MesQcTypeEnum.class)
    private Integer qcType;

    @Schema(description = "检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "检验单 ID 不能为空")
    private Long qcId;

    @Schema(description = "检验行 ID", example = "200")
    private Long lineId;

}
