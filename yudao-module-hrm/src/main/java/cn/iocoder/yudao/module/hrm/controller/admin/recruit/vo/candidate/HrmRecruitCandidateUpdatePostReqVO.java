package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘候选人职位修改 Request VO")
@Data
public class HrmRecruitCandidateUpdatePostReqVO {

    @Schema(description = "候选人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "候选人编号不能为空")
    private Long id;

    @Schema(description = "职位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "职位编号不能为空")
    private Long postId;

}
