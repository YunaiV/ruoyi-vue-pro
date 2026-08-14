package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 招聘候选人清理 Request VO")
@Data
public class HrmRecruitCandidateCleanReqVO {

    @Schema(description = "候选人状态数组", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[1, 2, 3, 4]")
    @NotEmpty(message = "候选人状态不能为空")
    private List<@NotNull(message = "候选人状态不能为空")
            @InEnum(value = HrmRecruitCandidateStatusEnum.class, message = "候选人状态必须是 {value}") Integer> statuses;

    @Schema(description = "状态持续天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "状态持续天数不能为空")
    @Positive(message = "状态持续天数必须大于 0")
    private Integer days;

}
