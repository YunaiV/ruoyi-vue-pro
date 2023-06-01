package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import cn.iocoder.yudao.module.jl.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 实验名目 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CategoryRespVO extends CategoryBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10130")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "历史操作次数", example = "0")
    @NotNull(message = "历史操作次数")
    private Integer actionCount;

    @Schema(description = "擅长的实验人员")
    private Set<User> skillUsers = new HashSet<>();
}
