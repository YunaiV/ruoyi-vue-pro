package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘候选人渠道修改 Request VO")
@Data
public class HrmRecruitCandidateUpdateChannelReqVO {

    @Schema(description = "候选人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "候选人编号不能为空")
    private Long id;

    @Schema(description = "招聘渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "招聘渠道编号不能为空")
    private Long channelId;

}
