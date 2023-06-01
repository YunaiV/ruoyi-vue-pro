package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 实验名目的擅长人员 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CategorySkillUserRespVO extends CategorySkillUserBaseVO {

    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29755")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
