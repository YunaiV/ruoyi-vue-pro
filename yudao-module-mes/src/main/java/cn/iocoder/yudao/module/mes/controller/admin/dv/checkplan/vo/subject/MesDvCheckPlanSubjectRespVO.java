package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 点检保养方案项目 Response VO")
@Data
public class MesDvCheckPlanSubjectRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long planId;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long subjectId;

    @Schema(description = "项目编码", example = "CHK001")
    private String subjectCode;

    @Schema(description = "项目名称", example = "油温检查")
    private String subjectName;

    @Schema(description = "项目类型", example = "1")
    private Integer subjectType;

    @Schema(description = "项目内容", example = "检查油温是否正常")
    private String subjectContent;

    @Schema(description = "标准", example = "40-60°C")
    private String subjectStandard;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
