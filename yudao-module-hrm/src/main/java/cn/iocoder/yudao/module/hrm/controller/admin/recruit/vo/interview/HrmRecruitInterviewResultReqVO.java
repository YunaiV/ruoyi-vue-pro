package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview;

import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.equalsAny;

@Schema(description = "管理后台 - HRM 招聘面试结果 Request VO")
@Data
public class HrmRecruitInterviewResultReqVO {

    @Schema(description = "面试编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "面试编号不能为空")
    private Long id;

    @Schema(description = "面试结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "面试结果不能为空")
    private Integer result;

    @Schema(description = "评价", example = "基础扎实")
    @Size(max = 255, message = "评价长度不能超过 255 个字符")
    private String evaluate;

    @Schema(description = "取消原因", example = "候选人临时改期")
    @Size(max = 255, message = "取消原因长度不能超过 255 个字符")
    private String cancelReason;

    @AssertTrue(message = "面试结果必须是通过、未通过或取消")
    @JsonIgnore
    public boolean isResultValid() {
        return equalsAny(result, null, HrmRecruitInterviewResultEnum.PASS.getResult(),
                HrmRecruitInterviewResultEnum.NOT_PASS.getResult(), HrmRecruitInterviewResultEnum.CANCEL.getResult());
    }

}
