package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘候选人状态修改 Request VO")
@Data
public class HrmRecruitCandidateUpdateStatusReqVO {

    @Schema(description = "候选人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "候选人编号不能为空")
    private Long id;

    @Schema(description = "候选人状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "候选人状态不能为空")
    @InEnum(value = HrmRecruitCandidateStatusEnum.class, message = "候选人状态必须是 {value}")
    private Integer status;

}
