package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 技能 Response VO")
@Data
public class AiSkillRespVO {

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "技能名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "order-analysis")
    private String name;

    @Schema(description = "技能描述，以 SKILL.md 头信息为准", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单统计口径与报表格式")
    private String description;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0.0")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "订单相关")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    // ========== 关联字段 ==========

    @Schema(description = "文件数量", example = "3")
    private Integer fileCount;

    @Schema(description = "引用的角色名称列表", example = "[\"订单助手\"]")
    private List<String> roleNames;

}
