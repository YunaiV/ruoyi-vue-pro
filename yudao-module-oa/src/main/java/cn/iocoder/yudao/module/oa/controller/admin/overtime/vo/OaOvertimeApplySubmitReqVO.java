package cn.iocoder.yudao.module.oa.controller.admin.overtime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 加班申请提交 Request VO")
@Data
public class OaOvertimeApplySubmitReqVO {

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "申请编号不能为空")
    private Long id;

    @Schema(description = "发起人自选审批人")
    private Map<String, List<Long>> startUserSelectAssignees;

}
