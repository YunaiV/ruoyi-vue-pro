package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 SKILL 内容文件 Response VO")
@Data
public class Ai1SkillFileRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "SKILL 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long skillId;

    @Schema(description = "父目录编号，0 为根级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "SKILL.md")
    private String name;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "文件类型，即扩展名；目录为空", example = "md")
    private String fileType;

    @Schema(description = "文件内容；文件树中不返回，编辑时通过获取接口加载", example = "# SKILL")
    private String content;

    @Schema(description = "是否固定：固定节点不可删除、改名、移动", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean locked;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
