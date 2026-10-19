package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 项目模板 Response VO")
@Data
public class PmsProjectTemplateRespVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "敏捷研发模板")
    private String name;

    @Schema(description = "模板描述", example = "适用于标准敏捷研发项目")
    private String description;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer projectType;

    @Schema(description = "模板状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "启用的工作项类型列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> itemTypes;

    @Schema(description = "工作项状态模板列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsProjectTemplateSaveReqVO.StatusTemplate> statuses;

    @Schema(description = "看板列模板列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsProjectTemplateSaveReqVO.BoardTemplate> boards;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
