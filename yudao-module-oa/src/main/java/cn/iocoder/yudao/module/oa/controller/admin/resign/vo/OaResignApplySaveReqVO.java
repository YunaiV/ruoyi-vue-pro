package cn.iocoder.yudao.module.oa.controller.admin.resign.vo;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

@Schema(description = "管理后台 - 离职申请新增/修改 Request VO")
@Data
public class OaResignApplySaveReqVO {

    @Schema(description = "申请编号", example = "1024")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "离职申请")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.APPLY_URGENCY, message = "紧急程度必须是 {value}")
    private Integer urgency;

    @Schema(description = "工作交接人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工作交接人不能为空")
    private Long handoverUserId;

    @Schema(description = "未完成事宜", requiredMode = Schema.RequiredMode.REQUIRED, example = "待移交客户资料与项目文档")
    @NotBlank(message = "未完成事宜不能为空")
    private String unfinishedWork;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "因个人职业发展规划申请离职")
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 5000, message = "申请原因不能超过 5000 个字符")
    private String reason;

    @Schema(description = "是否有费用报销未完成", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否有费用报销未完成不能为空")
    private Boolean hasPendingReimbursement;


}
