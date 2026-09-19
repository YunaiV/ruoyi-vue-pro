package cn.iocoder.yudao.module.oa.controller.admin.regular.vo;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 转正申请新增/修改 Request VO")
@Data
public class OaRegularApplySaveReqVO {

    @Schema(description = "申请编号", example = "1024")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "试用期转正申请")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.APPLY_URGENCY, message = "紧急程度必须是 {value}")
    private Integer urgency;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "试用期心得", requiredMode = Schema.RequiredMode.REQUIRED, example = "已熟悉团队协作流程并完成安排的任务")
    @NotBlank(message = "试用期心得不能为空")
    @Size(max = 255, message = "试用期心得不能超过 255 个字符")
    private String experience;

    @Schema(description = "岗位职责理解", requiredMode = Schema.RequiredMode.REQUIRED, example = "负责项目开发、交付及日常维护")
    @NotBlank(message = "岗位职责理解不能为空")
    @Size(max = 255, message = "岗位职责理解不能超过 255 个字符")
    private String understanding;

    @Schema(description = "试用期成长", requiredMode = Schema.RequiredMode.REQUIRED, example = "能够独立完成需求开发与问题排查")
    @NotBlank(message = "试用期成长不能为空")
    @Size(max = 255, message = "试用期成长不能超过 255 个字符")
    private String growth;

    @Schema(description = "目前不足", requiredMode = Schema.RequiredMode.REQUIRED, example = "跨部门沟通效率仍需提升")
    @NotBlank(message = "目前不足不能为空")
    @Size(max = 255, message = "目前不足不能超过 255 个字符")
    private String deficiency;

    @Schema(description = "工作改进", requiredMode = Schema.RequiredMode.REQUIRED, example = "完善任务拆分并每日跟进进度")
    @NotBlank(message = "工作改进不能为空")
    @Size(max = 255, message = "工作改进不能超过 255 个字符")
    private String improvement;

    @Schema(description = "产品意见建议", requiredMode = Schema.RequiredMode.REQUIRED, example = "建议简化报销填写流程")
    @NotBlank(message = "产品意见建议不能为空")
    @Size(max = 255, message = "产品意见建议不能超过 255 个字符")
    private String suggestion;

    @AssertTrue(message = "结束时间不能早于开始时间")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !endTime.isBefore(startTime);
    }

}
