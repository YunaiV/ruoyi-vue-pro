package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 SKILL Response VO")
@Data
public class Ai1SkillRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "pdf-reader")
    private String name;

    @Schema(description = "描述", example = "读取并总结 PDF 文件")
    private String description;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
