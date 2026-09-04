package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemDefectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 工作项新增/修改 Request VO")
@Data
public class PmsWorkItemSaveReqVO {

    @Schema(description = "工作项编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "工作项类型不能为空")
    @InEnum(PmsWorkItemTypeEnum.class)
    private Integer type;

    @Schema(description = "工作项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成登录页")
    @NotBlank(message = "工作项标题不能为空")
    @Size(max = 100, message = "工作项标题不能超过 100 个字符")
    private String name;

    @Schema(description = "工作项描述", example = "实现账号密码登录")
    @Size(max = 5000, message = "工作项描述不能超过 5000 个字符")
    private String description;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "优先级不能为空")
    @InEnum(PmsWorkItemPriorityEnum.class)
    private Integer priority;

    @Schema(description = "负责人用户编号", example = "1024")
    private Long assigneeUserId;

    @Schema(description = "参与人用户编号列表", example = "[1024, 2048]")
    private List<Long> memberUserIds;

    @Schema(description = "所属迭代编号", example = "1024")
    private Long iterationId;

    @Schema(description = "父工作项编号", example = "1024")
    private Long parentId;

    @Schema(description = "关联需求编号", example = "1024")
    private Long relatedRequirementId;

    @Schema(description = "缺陷类型", example = "1")
    @InEnum(PmsWorkItemDefectTypeEnum.class)
    private Integer defectType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "预估工时，单位：小时", example = "8")
    @Min(value = 0, message = "预估工时不能小于 0")
    private Integer estimatedHours;

    @Schema(description = "完成进度", example = "50")
    @NotNull(message = "完成进度不能为空")
    @Min(value = 0, message = "完成进度不能小于 0")
    @Max(value = 100, message = "完成进度不能大于 100")
    private Integer progress;

    @Schema(description = "附件地址列表")
    private List<String> fileUrls;

    @Schema(description = "标签编号列表", example = "[1024, 2048]")
    private List<Long> labelIds;

    @Schema(description = "创建时同时新增的子工作项标题列表")
    private List<@NotBlank(message = "子工作项标题不能为空")
            @Size(max = 100, message = "子工作项标题不能超过 100 个字符") String> childWorkItemNames;

    @Schema(description = "创建时登记的实际投入工时，单位：小时", example = "2")
    @Min(value = 1, message = "实际投入工时必须大于 0")
    private Integer actualHours;

    @Schema(description = "创建时登记的剩余工时，单位：小时", example = "6")
    @Min(value = 0, message = "剩余工时不能小于 0")
    private Integer remainingHours;

    @AssertTrue(message = "工作项结束时间必须晚于开始时间")
    public boolean isTimeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

    @AssertTrue(message = "填写剩余工时时必须同时填写实际投入工时")
    public boolean isWorkLogValid() {
        return remainingHours == null || actualHours != null;
    }

}
