package cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 出差报销提交 Request VO")
@Data
public class OaTravelReimbursementSubmitReqVO {

    @Schema(description = "单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "单据编号不能为空")
    private Long id;

    @Schema(description = "发起人自选审批人")
    private Map<String, List<Long>> startUserSelectAssignees;

}
