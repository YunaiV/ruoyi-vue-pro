package cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 编码规则组成新增/修改 Request VO")
@Data
public class MesMdAutoCodePartSaveReqVO {

    @Schema(description = "分段 ID", example = "1024")
    private Long id;

    @Schema(description = "规则 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "规则 ID 不能为空")
    private Long ruleId;

    @Schema(description = "分段序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分段序号不能为空")
    private Integer sort;

    @Schema(description = "分段类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分段类型不能为空")
    private Integer type;

    @Schema(description = "分段长度", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "分段长度不能为空")
    private Integer length;

    @Schema(description = "日期格式", example = "yyyyMMdd")
    private String dateFormat;

    @Schema(description = "固定字符", example = "ITEM_")
    private String fixCharacter;

    @Schema(description = "流水号起始值", example = "1")
    private Integer serialStartNo;

    @Schema(description = "流水号步长", example = "1")
    private Integer serialStep;

    @Schema(description = "流水号是否循环", example = "true")
    private Boolean cycleFlag;

    @Schema(description = "循环方式", example = "3")
    private Integer cycleMethod;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
