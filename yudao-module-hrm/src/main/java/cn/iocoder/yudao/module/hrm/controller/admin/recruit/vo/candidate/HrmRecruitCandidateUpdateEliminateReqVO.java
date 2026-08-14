package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘候选人淘汰 Request VO")
@Data
public class HrmRecruitCandidateUpdateEliminateReqVO {

    @Schema(description = "候选人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "候选人编号不能为空")
    private Long id;

    @Schema(description = "淘汰原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "技能不匹配")
    @NotBlank(message = "淘汰原因不能为空")
    @Size(max = 255, message = "淘汰原因长度不能超过 255 个字符")
    private String eliminate;

    @Schema(description = "备注", example = "二面未通过")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
